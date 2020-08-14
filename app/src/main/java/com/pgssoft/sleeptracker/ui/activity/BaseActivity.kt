package com.pgssoft.sleeptracker.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.pgssoft.sleeptracker.errorhandling.ErrorCode
import com.pgssoft.sleeptracker.errorhandling.Failure
import com.pgssoft.sleeptracker.errorhandling.Result
import com.pgssoft.sleeptracker.errorhandling.Success
import com.pgssoft.sleeptracker.services.FitService
import com.pgssoft.sleeptracker.services.PermissionsService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {
    enum class RequestCode(val code: Int) {
        RequestRuntimePermission(1),
        RequestOAuthPermission(2),
        RequestSignIn(3)
    }

    val fitService by inject<FitService>()
    val permissionsService by inject<PermissionsService>()

    var currentCode: RequestCode? = null
    var pendingResults = mutableMapOf<Int, CompletableDeferred<Result<Intent>>>()
    var pendingPermissionResult: CompletableDeferred<Result<List<String>>>? = null

    override fun onStart() {
        super.onStart()

        fitService.activity = this
        permissionsService.activity = this
    }

    override fun onStop() {
        if (fitService.activity == this) {
            fitService.activity = null
        }
        if (permissionsService.activity == this) {
            permissionsService.activity = null
        }

        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        pendingResults[requestCode]?.let {
            it.complete(if (resultCode == Activity.RESULT_OK) Success(data!!) else Failure(ErrorCode.FailedToGetActivityResult))
            pendingResults.remove(requestCode)
        }

        currentCode = null
    }

    fun launchIntentForResult(requestCode: RequestCode, intent: Intent): Deferred<Result<Intent>> {
        val result = CompletableDeferred<Result<Intent>>()

        return try {
            startActivityForResult(intent, requestCode.code)

            currentCode = requestCode
            pendingResults[requestCode.code] = result

            result
        } catch (ex: Exception) {
            result.complete(Failure(ErrorCode.FailedToGetActivityResult, ex))
            result
        }
    }

    fun launchForResult(requestCode: RequestCode, action: (BaseActivity) -> Unit): Deferred<Result<Intent>> {
        val result = CompletableDeferred<Result<Intent>>()

        return try {
            currentCode = requestCode
            pendingResults[requestCode.code] = result

            action.invoke(this)

            result
        } catch (ex: Exception) {
            result.complete(Failure(ErrorCode.FailedToGetActivityResult, ex))
            result
        }
    }

    fun requestPermissions(permissions: List<String>): Deferred<Result<List<String>>> {
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), RequestCode.RequestRuntimePermission.code);

        pendingPermissionResult = CompletableDeferred()
        return pendingPermissionResult!!
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        pendingPermissionResult?.let { continuation ->
            pendingPermissionResult = null

            val deniedPermissions = permissions.filterIndexed { index, value -> grantResults[index] != PackageManager.PERMISSION_GRANTED }
            continuation.complete(Success(deniedPermissions))
        }
    }
}
