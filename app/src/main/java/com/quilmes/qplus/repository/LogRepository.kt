package com.quilmes.qplus.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import com.elstatgroup.elstat.sdk.api.NexoLog
import com.elstatgroup.elstat.sdk.errror.NexoError
import com.elstatgroup.elstat.sdk.model.NexoAuthenticatedUser
import com.quilmes.qplus.model.SingleResult

class LogRepository {

    private val logUriResult = MutableLiveData<SingleResult<Uri>>()
    private val getLogCallback: NexoLog.NexoGetLogFileCallback = object: NexoLog.NexoGetLogFileCallback {

        override fun onLogFile(logUri: Uri) {
            logUriResult.postValue(SingleResult(logUri))
        }

        override fun onError(error: NexoError) {
            logUriResult.postValue(SingleResult(error))
        }
    }

    fun getLogUri(context: Context, authenticatedUser: NexoAuthenticatedUser): LiveData<SingleResult<Uri>> {
        NexoLog.getLogFile(context, authenticatedUser, getLogCallback)
        return logUriResult
    }

}