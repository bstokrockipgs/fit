package com.pgssoft.sleeptracker.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.pgssoft.sleeptracker.errorhandling.ErrorCode
import com.pgssoft.sleeptracker.errorhandling.Failure
import com.pgssoft.sleeptracker.errorhandling.Result
import com.pgssoft.sleeptracker.errorhandling.Success
import com.pgssoft.sleeptracker.ui.activity.BaseActivity

class PermissionsService(private val context: Context) {
    var activity: BaseActivity? = null

    /**
     * @return list of denied permissions
     */
    fun checkPermissions(permissions: List<String>) = activity?.run {
        val result = arrayListOf<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                result.add(permission)
            }
        }

        Success(result)
    } ?: Failure<List<String>>(ErrorCode.FailedToCheckPermissions)

    suspend fun requestRuntimePermissions(permissions: List<String>): Result<List<String>> {
        val missingPermissions = when (val temp = checkPermissions(permissions)) {
            is Failure -> return Failure(ErrorCode.FailedToCheckPermissions)
            is Success -> if (temp.value.isEmpty()) {
                return Success(emptyList())
            } else {
                temp.value
            }
        }

        return activity?.run {
            this.requestPermissions(missingPermissions).await()
        } ?: Failure(ErrorCode.FailedToRequestPermissions)
    }

    suspend fun requestScopeAccess(intent: Intent): Result<GoogleSignInAccount> = activity?.run {
        val activityResult = this.launchIntentForResult(BaseActivity.RequestCode.RequestOAuthPermission, intent)
        when (val temp = activityResult.await()) {
            is Failure -> Failure<GoogleSignInAccount>(temp)
            is Success -> {
                temp.value?.getParcelableExtra<GoogleSignInAccount>("googleSignInAccount")?.run {
                    Success(this)
                } ?: Failure(ErrorCode.FailedToGainScopeAccess)
            }
        }
    } ?: Failure(ErrorCode.FailedToRequestPermissions)

    suspend fun requestSignIn(intent: Intent): Result<GoogleSignInAccount> = activity?.run {
        val activityResult = this.launchIntentForResult(BaseActivity.RequestCode.RequestSignIn, intent)
        when (val temp = activityResult.await()) {
            is Failure -> Failure<GoogleSignInAccount>(temp)
            is Success -> {
                temp.value?.getParcelableExtra<GoogleSignInAccount>("googleSignInAccount")?.run {
                    Success(this)
                } ?: Failure(ErrorCode.SignInFailed)
            }
        }
    } ?: Failure(ErrorCode.SignInFailed)
}