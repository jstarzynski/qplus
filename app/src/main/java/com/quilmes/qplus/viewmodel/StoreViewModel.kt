package com.quilmes.qplus.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.elstatgroup.elstat.sdk.model.NexoAuthenticatedUser
import com.quilmes.qplus.controller.CheckInController
import com.quilmes.qplus.model.NexoStoreCooler

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    fun checkIn(authenticatedUser: NexoAuthenticatedUser, storeId: String) {
        CheckInController.checkIn(getApplication(), authenticatedUser, storeId)
    }

    fun checkOut(storeId: String) {
        CheckInController.checkOut(getApplication(), storeId)
    }

    fun coolers(storeId: String): LiveData<List<NexoStoreCooler>> = CheckInController.getStream(storeId)

}