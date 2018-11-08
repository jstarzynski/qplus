package com.quilmes.qplus.model

import com.elstatgroup.elstat.sdk.errror.NexoError

class SingleResult<T> {

    private var result: T?
    private var error: NexoError?

    constructor(result: T) {
        this.result = result
        error = null
    }

    constructor(error: NexoError) {
        this.error = error
        result = null
    }

    fun isSuccessful() = result != null && error == null
    fun getResult(): T = result ?: throw IllegalStateException("Result not successful!")
    fun getError() = error ?: NexoError(NexoError.NexoErrorType.UNKOWN_ERROR)

}