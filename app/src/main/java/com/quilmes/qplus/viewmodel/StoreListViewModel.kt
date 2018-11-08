package com.quilmes.qplus.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.quilmes.qplus.repository.CloudRepository

class StoreListViewModel(application: Application) : AndroidViewModel(application) {

    private val cloudRepository = CloudRepository()
    val authenticationStream = cloudRepository.authenticateUser(application)

}