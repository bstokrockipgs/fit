package com.pgssoft.sleeptracker.errorhandling

enum class ErrorCode {
    //General
    UnknownError,
    ResolvableIssueOccurred,
    TaskCancelled,
    FailedToGetActivityResult,

    //Auth
    SignInRequired,
    SignInFailed,

    //Permissions
    AdditionalPermissionsRequired,
    FailedToGainScopeAccess,
    FailedToCheckPermissions,
    FailedToRequestPermissions
}