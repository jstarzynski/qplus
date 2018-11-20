package com.quilmes.qplus.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.net.Uri
import com.elstatgroup.elstat.sdk.errror.NexoError
import com.elstatgroup.elstat.sdk.model.NexoAuthenticatedUser
import com.quilmes.qplus.model.SingleResult
import com.quilmes.qplus.repository.AuthRepository
import com.quilmes.qplus.repository.LogRepository

class StoreListViewModel(application: Application) : AndroidViewModel(application) {

    private val cloudRepository = AuthRepository()
    private val logRepository = LogRepository()

    private val logUriStream = MutableLiveData<SingleResult<Uri>>()
    val authenticationStream = cloudRepository.authenticateUser(application)

    fun getLogUriStream(): LiveData<SingleResult<Uri>> = logUriStream

    fun retryAuthentication() {
        cloudRepository.authenticateUser(getApplication())
    }

    fun generateUriForLog(context: Context, authenticatedUser: NexoAuthenticatedUser) {
        val result = logRepository.getLogUri(context, authenticatedUser)
        result.observeForever(object : Observer<SingleResult<Uri>> {
            override fun onChanged(response: SingleResult<Uri>?) {
                result.removeObserver(this)
                if (response != null) logUriStream.postValue(response)
                else logUriStream.postValue(SingleResult(NexoError(NexoError.NexoErrorType.UNKOWN_ERROR)))
            }
        })
    }
}
