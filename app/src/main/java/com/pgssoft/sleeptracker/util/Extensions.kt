package com.pgssoft.sleeptracker.util

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.Task
import com.pgssoft.sleeptracker.errorhandling.ErrorCode
import com.pgssoft.sleeptracker.errorhandling.Failure
import com.pgssoft.sleeptracker.errorhandling.Result
import com.pgssoft.sleeptracker.errorhandling.Success
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun <TResult> Task<TResult>.await() = suspendCoroutine<Result<TResult>> { cont ->
    addOnSuccessListener {
        cont.resume(Success(it)) }
    addOnCanceledListener {
        cont.resume(Failure(ErrorCode.TaskCancelled)) }
    addOnFailureListener {
        when (it) {
            is ResolvableApiException -> cont.resume(Failure(ErrorCode.ResolvableIssueOccurred, it))
            is ApiException -> cont.resume(Failure( when (it.statusCode) {
                CommonStatusCodes.SIGN_IN_REQUIRED -> ErrorCode.SignInRequired
                CommonStatusCodes.DEVELOPER_ERROR -> ErrorCode.AdditionalPermissionsRequired
                //OAuth permissions missing
                5000 -> ErrorCode.ResolvableIssueOccurred
                else -> ErrorCode.UnknownError
            }, it))
            else -> cont.resume(Failure(ErrorCode.UnknownError))
        }
    }
}