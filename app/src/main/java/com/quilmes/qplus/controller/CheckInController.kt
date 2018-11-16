package com.quilmes.qplus.controller

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import com.elstatgroup.elstat.sdk.api.NexoAutoSyncListener
import com.elstatgroup.elstat.sdk.api.NexoCoolerProgressListener
import com.elstatgroup.elstat.sdk.api.NexoSync
import com.elstatgroup.elstat.sdk.errror.NexoError
import com.elstatgroup.elstat.sdk.model.NexoAuthenticatedUser
import com.elstatgroup.elstat.sdk.model.NexoCooler
import com.elstatgroup.elstat.sdk.model.NexoProgress
import com.elstatgroup.elstat.sdk.model.identifier.NexoBluetoothIdentifier
import com.quilmes.qplus.model.NexoStoreCooler
import com.quilmes.qplus.model.NexoStoreCoolerStatus
import com.quilmes.qplus.model.NexoStoreState
import com.quilmes.qplus.model.SingleResult
import com.quilmes.qplus.repository.CloudRepository

object CheckInController {

    private val cloudRepository = CloudRepository()

    private val processedStoreUpdates = mutableMapOf<NexoBluetoothIdentifier, String>()
    private val autoSyncListenersMap = mutableMapOf<String, NexoAutoSyncListener>()
    private val syncProgressListenersMap = mutableMapOf<String, NexoCoolerProgressListener>()
    private val nexoStoreStateMap = mutableMapOf<String, NexoStoreState>()

    private val nexoStoreCoolersStreamMap = mutableMapOf<String, MutableLiveData<List<NexoStoreCooler>>>()

    fun checkIn(context: Context, authenticatedUser: NexoAuthenticatedUser, storeId: String) {

        nexoStoreStateMap[storeId] = NexoStoreState()

        retrieveControllersFromCloud(context, authenticatedUser, storeId)

        NexoSync.getInstance().clearAutoSyncCache(context, authenticatedUser)
        generateAutoSyncListener(context, authenticatedUser, storeId).let {
            autoSyncListenersMap[storeId] = it
            NexoSync.getInstance().addAutoSyncListener(it)
        }
        generateProgressSyncListener(context, authenticatedUser, storeId).let {
            syncProgressListenersMap[storeId] = it
            NexoSync.getInstance().addSyncListener(it)
        }
        NexoSync.getInstance().beginAutoSync(context, authenticatedUser)
    }

    fun checkOut(context: Context, storeId: String) {
        NexoSync.getInstance().stopAutoSync(context)
        autoSyncListenersMap.remove(storeId)?.let { NexoSync.getInstance().removeAutoSyncListener(it) }
        syncProgressListenersMap.remove(storeId)?.let { NexoSync.getInstance().removeSyncListener(it) }
    }

    fun getStream(storeId: String) = nexoStoreCoolersStreamMap[storeId] ?: MutableLiveData<List<NexoStoreCooler>>().also { nexoStoreCoolersStreamMap[storeId] = it }

    private fun notifyCoolersStateChanged(context: Context, authenticatedUser: NexoAuthenticatedUser, storeId: String) {
        nexoStoreStateMap[storeId]?.also {
            val coolers = mutableMapOf<String, NexoStoreCooler>()

            it.commissionedControllers.minus(it.expectedControllers).forEach {
                updateStoreId(context, authenticatedUser, storeId, it)
            }

            it.detectedControllers.forEach { coolers[it.bluetoothId] = NexoStoreCooler(it.bluetoothId, NexoStoreCoolerStatus.PENDING) }
            it.expectedControllers.minus(it.detectedControllers).forEach { coolers[it.bluetoothId] = NexoStoreCooler(it.bluetoothId, NexoStoreCoolerStatus.NOT_FOUND) }

            it.syncProgress.forEach { (identifier, progress) -> coolers[identifier.bluetoothId] = NexoStoreCooler(identifier.bluetoothId, NexoStoreCoolerStatus.PENDING, progress)}
            it.syncedControllers.forEach { coolers[it.bluetoothId] =  NexoStoreCooler(it.bluetoothId, NexoStoreCoolerStatus.SYNCED) }
            it.decommissionedControllers.forEach { coolers[it.bluetoothId] =  NexoStoreCooler(it.bluetoothId, NexoStoreCoolerStatus.REQUIRES_COMMISSIONING) }

            getStream(storeId).postValue(coolers.values.sortedBy { it.status.ordinal })
        }
    }

    private fun updateStoreId(context: Context, authenticatedUser: NexoAuthenticatedUser, storeId: String, bluetoothIdentifier: NexoBluetoothIdentifier) {
        if (processedStoreUpdates[bluetoothIdentifier] != storeId) {
            cloudRepository.updateStoreId(context, authenticatedUser, bluetoothIdentifier, storeId)
            processedStoreUpdates[bluetoothIdentifier] = storeId
        }
    }

    private fun retrieveControllersFromCloud(context: Context, authenticatedUser: NexoAuthenticatedUser, storeId: String) {
        val result = cloudRepository.getControllersByStoreId(context, authenticatedUser, storeId)
        result.observeForever(object : Observer<SingleResult<List<NexoBluetoothIdentifier>>> {
            override fun onChanged(response: SingleResult<List<NexoBluetoothIdentifier>>?) {
                result.removeObserver(this)
                if (response?.isSuccessful() == true)
                    nexoStoreStateMap[storeId]?.let {
                        nexoStoreStateMap[storeId] = it.copy(expectedControllers = it.expectedControllers + response.getResult())
                        notifyCoolersStateChanged(context, authenticatedUser, storeId)
                    }
            }
        })
    }

    private fun generateAutoSyncListener(context: Context, authenticatedUser: NexoAuthenticatedUser, storeId: String) = object : NexoAutoSyncListener {

        override fun onCoolerLocated(devices: MutableCollection<NexoCooler>, locatedCooler: NexoBluetoothIdentifier) {
            nexoStoreStateMap[storeId]?.let {
                nexoStoreStateMap[storeId] = it.copy(detectedControllers = it.detectedControllers + locatedCooler)
                notifyCoolersStateChanged(context, authenticatedUser, storeId)
            }
        }

        override fun onError(nexoError: NexoError) {}

        override fun onCoolerUpdated(nexoCooler: NexoCooler) {

            when {
                nexoCooler.commissioningState == NexoCooler.CommissioningState.UNCOMMISSIONED -> nexoStoreStateMap[storeId]?.let {
                    nexoStoreStateMap[storeId] = it.copy(decommissionedControllers = it.decommissionedControllers + nexoCooler.bluetoothIdentifier)
                    notifyCoolersStateChanged(context, authenticatedUser, storeId)
                }
                else -> nexoStoreStateMap[storeId]?.let {
                    nexoStoreStateMap[storeId] = it.copy(commissionedControllers = it.commissionedControllers + nexoCooler.bluetoothIdentifier)
                    notifyCoolersStateChanged(context, authenticatedUser, storeId)
                }
            }

            if (nexoCooler.isSynced)
                nexoStoreStateMap[storeId]?.let {
                    nexoStoreStateMap[storeId] = it.copy(syncedControllers = it.syncedControllers + nexoCooler.bluetoothIdentifier)
                    notifyCoolersStateChanged(context, authenticatedUser, storeId)
                }
        }

        override fun onCoolerSyncProgress(identifier: NexoBluetoothIdentifier, nexoProgress: NexoProgress) {
            nexoStoreStateMap[storeId]?.let {
                nexoStoreStateMap[storeId] = it.copy(syncProgress = it.syncProgress + (identifier to nexoProgress.percents))
                notifyCoolersStateChanged(context, authenticatedUser, storeId)
            }
        }

        override fun onCoolerDisappeared(devices: MutableCollection<NexoCooler>, disappearedCooler: NexoBluetoothIdentifier) {}

    }

    private fun generateProgressSyncListener(context: Context, authenticatedUser: NexoAuthenticatedUser, storeId: String) = object : NexoCoolerProgressListener {

        override fun onError(nexoError: NexoError) {}

        override fun onCoolerProgress(nexoCooler: NexoCooler, progress: NexoProgress) {
            if (nexoCooler.isSynced
                    || (nexoCooler.commissioningState != NexoCooler.CommissioningState.UNCOMMISSIONED && progress.percents == 100))
                nexoStoreStateMap[storeId]?.let {
                    nexoStoreStateMap[storeId] = it.copy(syncedControllers = it.syncedControllers + nexoCooler.bluetoothIdentifier)
                    notifyCoolersStateChanged(context, authenticatedUser, storeId)
                }
        }

    }

}