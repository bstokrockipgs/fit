package com.pgssoft.sleeptracker.errorhandling

import android.util.Log

sealed class Result<T> {
    fun <R> map(converter: (T) -> R): Result<R> = when (this) {
        is Failure -> Failure(
            this
        )
        is Success -> Success(
            converter(this.value)
        )
    }

    suspend fun <R> suspendingAdapt(adapter: suspend (arg: T) -> Result<R>): Result<R> = when (this) {
        is Failure -> Failure(
            this
        )
        is Success -> adapter.invoke(this.value)
    }
}

data class Success<T>(val value: T): Result<T>()

data class Failure<T>(val error: ErrorCode, val throwable: Throwable? = null, val message: String? = null): Result<T>() {
    constructor(failure: Failure<*>) : this(failure.error, failure.throwable)

    fun print() {
        Log.e("ERROR", error.name)
        throwable?.printStackTrace()
    }
}