package com.quilmes.qplus.repository

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.provider.Settings
import com.elstatgroup.elstat.sdk.api.NexoCloud
import com.elstatgroup.elstat.sdk.errror.NexoError
import com.elstatgroup.elstat.sdk.model.NexoAuthenticatedUser
import com.quilmes.qplus.model.SingleResult


class CloudRepository {

    fun authenticateUser(context: Context): LiveData<SingleResult<NexoAuthenticatedUser>> {

        val result = MutableLiveData<SingleResult<NexoAuthenticatedUser>>()

        NexoCloud.authenticate(context, "${getUUID(context)}@quilmes.com", object: NexoCloud.NexoAuthenticateCallback {

            override fun onAuthenticate(user: NexoAuthenticatedUser) {
                result.postValue(SingleResult(user))
            }

            override fun onError(error: NexoError) {
                result.postValue(SingleResult(error))
            }
        })

        return result
    }

    @SuppressLint("HardwareIds")
    private fun getUUID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

}