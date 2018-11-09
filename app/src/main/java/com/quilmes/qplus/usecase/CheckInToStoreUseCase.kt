package com.quilmes.qplus.usecase

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import com.elstatgroup.elstat.sdk.api.NexoAutoSyncListener
import com.elstatgroup.elstat.sdk.api.NexoSync
import com.elstatgroup.elstat.sdk.errror.NexoError
import com.elstatgroup.elstat.sdk.model.NexoAuthenticatedUser
import com.elstatgroup.elstat.sdk.model.NexoCooler
import com.elstatgroup.elstat.sdk.model.NexoProgress
import com.elstatgroup.elstat.sdk.model.identifier.NexoBluetoothIdentifier
import com.quilmes.qplus.model.NexoStoreCooler
import com.quilmes.qplus.model.NexoStoreState
import com.quilmes.qplus.model.SingleResult
import com.quilmes.qplus.repository.CloudRepository

class CheckInToStoreUseCase {

    private val cloudRepository = CloudRepository()

    private val autoSyncListenersMap = mutableMapOf<String, NexoAutoSyncListener>()
    private val nexoStoreStateMap = mutableMapOf<String, NexoStoreState>()

    private val nexoStoreCoolersStreamMap = mutableMapOf<String, MutableLiveData<List<NexoStoreCooler>>>()

    fun checkIn(context: Context, authenticatedUser: NexoAuthenticatedUser, storeId: String) {

        nexoStoreStateMap[storeId] = NexoStoreState()
        nexoStoreCoolersStreamMap[storeId] = MutableLiveData()

        retrieveControllersFromCloud(context, authenticatedUser, storeId)

        NexoSync.getInstance().clearAutoSyncCache(context, authenticatedUser)
        generateAutoSyncListener(storeId).let {
            autoSyncListenersMap[storeId] = it
            NexoSync.getInstance().addAutoSyncListener(it)
        }
        NexoSync.getInstance().beginAutoSync(context, authenticatedUser)
    }

    fun checkOut(context: Context, storeId: String) {
        NexoSync.getInstance().stopAutoSync(context)
        autoSyncListenersMap.remove(storeId)?.let { NexoSync.getInstance().removeAutoSyncListener(it) }
    }

    private fun streamCoolersState() {

    }

    private fun retrieveControllersFromCloud(context: Context, authenticatedUser: NexoAuthenticatedUser, storeId: String) {
        val result = cloudRepository.getControllersByStoreId(context, authenticatedUser, storeId)
        result.observeForever(object : Observer<SingleResult<List<NexoBluetoothIdentifier>>> {
            override fun onChanged(response: SingleResult<List<NexoBluetoothIdentifier>>?) {
                result.removeObserver(this)
                if (response?.isSuccessful() == true)
                    nexoStoreStateMap[storeId]?.let {
                        nexoStoreStateMap[storeId] = it.copy(expectedControllers = it.expectedControllers + response.getResult())
                    }
            }
        })
    }

    private fun generateAutoSyncListener(storeId: String) = object : NexoAutoSyncListener {

        override fun onCoolerLocated(devices: MutableCollection<NexoCooler>, locatedCooler: NexoBluetoothIdentifier) {
            nexoStoreStateMap[storeId]?.let {
                nexoStoreStateMap[storeId] = it.copy(detectedControllers = it.detectedControllers + locatedCooler)
            }
        }

        override fun onError(nexoError: NexoError) {}

        override fun onCoolerUpdated(nexoCooler: NexoCooler) {
            if (nexoCooler.commissioningState == NexoCooler.CommissioningState.UNCOMMISSIONED)
                nexoStoreStateMap[storeId]?.let {
                    nexoStoreStateMap[storeId] = it.copy(decommissionedControllers = it.decommissionedControllers + nexoCooler.bluetoothIdentifier)
                }
            else if (nexoCooler.isSynced)
                nexoStoreStateMap[storeId]?.let {
                    nexoStoreStateMap[storeId] = it.copy(syncedControllers = it.syncedControllers + nexoCooler.bluetoothIdentifier)
                }
        }

        override fun onCoolerSyncProgress(identifier: NexoBluetoothIdentifier, nexoProgress: NexoProgress) {
            nexoStoreStateMap[storeId]?.let {
                nexoStoreStateMap[storeId] = it.copy(syncProgress = it.syncProgress + (identifier to nexoProgress.percents))
            }
        }

        override fun onCoolerDisappeared(devices: MutableCollection<NexoCooler>, disappearedCooler: NexoBluetoothIdentifier) {}

    }

}