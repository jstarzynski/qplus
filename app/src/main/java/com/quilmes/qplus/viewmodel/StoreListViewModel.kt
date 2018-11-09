package com.quilmes.qplus.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.quilmes.qplus.repository.AuthRepository

class StoreListViewModel(application: Application) : AndroidViewModel(application) {

    private val cloudRepository = AuthRepository()
    val authenticationStream = cloudRepository.authenticateUser(application)

    fun retryAuthentication() {
        cloudRepository.authenticateUser(getApplication())
    }

}