package com.quilmes.qplus.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.elstatgroup.elstat.sdk.api.NexoCloud
import com.elstatgroup.elstat.sdk.errror.NexoError
import com.elstatgroup.elstat.sdk.model.NexoAuthenticatedUser
import com.elstatgroup.elstat.sdk.model.identifier.NexoBluetoothIdentifier
import com.elstatgroup.elstat.sdk.model.identifier.NexoStoreIdentifier
import com.elstatgroup.elstat.sdk.model.metadata.NexoCoolerMetadata
import com.quilmes.qplus.model.SingleResult

class CloudRepository {

    private val metadataCallbackMap = mutableMapOf<String, NexoCloud.NexoCoolerMetadataCallback>()
    private val controllersByStoreResultsMap = mutableMapOf<String, MutableLiveData<SingleResult<List<NexoBluetoothIdentifier>>>>()

    fun getControllersByStoreId(context: Context, authenticatedUser: NexoAuthenticatedUser, storeId: String): LiveData<SingleResult<List<NexoBluetoothIdentifier>>> {
        val result = generateControllersByStoreResultStream(storeId)
        NexoCloud.getNexoCoolerMetadata(context, authenticatedUser, listOf(NexoStoreIdentifier(storeId)), generateMetadataCallback(storeId))
        return result
    }

    private fun generateControllersByStoreResultStream(storeId: String) = controllersByStoreResultsMap[storeId] ?: MutableLiveData()

    private fun generateMetadataCallback(storeId: String) = metadataCallbackMap[storeId]
            ?: object : NexoCloud.NexoCoolerMetadataCallback {

                override fun onError(nexoError: NexoError) {
                    controllersByStoreResultsMap[storeId]?.postValue(SingleResult(nexoError))
                }

                override fun onMetadata(coolersMetada: MutableList<NexoCoolerMetadata>) {
                    controllersByStoreResultsMap[storeId]?.postValue(SingleResult(coolersMetada.map { it.bluetoothIdentifier }))
                }

            }.also { metadataCallbackMap[storeId] = it }

}