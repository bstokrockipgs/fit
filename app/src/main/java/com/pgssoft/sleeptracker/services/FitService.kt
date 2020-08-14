package com.pgssoft.sleeptracker.services

import android.Manifest
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.*
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.SessionInsertRequest
import com.google.android.gms.fitness.request.SessionReadRequest
import com.pgssoft.sleeptracker.errorhandling.ErrorCode
import com.pgssoft.sleeptracker.errorhandling.Failure
import com.pgssoft.sleeptracker.errorhandling.Result
import com.pgssoft.sleeptracker.errorhandling.Success
import com.pgssoft.sleeptracker.model.SleepEntry
import com.pgssoft.sleeptracker.model.SleepSession
import com.pgssoft.sleeptracker.ui.activity.BaseActivity
import com.pgssoft.sleeptracker.util.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTimeConstants
import java.util.concurrent.TimeUnit


class FitService(private val context2: Context, private val permissionsService: PermissionsService) {
    var activity: BaseActivity? = null

    private val fitScope by lazy {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_NUTRITION, FitnessOptions.ACCESS_WRITE)
            .build()
    }

    private val client: GoogleSignInClient
        get() = GoogleSignIn.getClient(activity!!, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .build())

    val googleAccount: GoogleSignInAccount?
        get() = GoogleSignIn.getLastSignedInAccount(context2)

    val sessionClient: SessionsClient?
        get() = googleAccount?.run { Fitness.getSessionsClient(context2, this) }

    val historyClient: HistoryClient?
        get() = googleAccount?.run { Fitness.getHistoryClient(context2, this) }

    suspend fun readSleepData(): Result<List<SleepSession>> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val readRequest = SessionReadRequest.Builder()
            .read(DataType.TYPE_ACTIVITY_SEGMENT)
            .setTimeInterval(now - 180L * DateTimeConstants.MILLIS_PER_DAY, now, TimeUnit.MILLISECONDS)
            .build()

        val response = when (val temp = wrapFitRequest { sessionClient?.readSession(readRequest)?.await() }) {
            is Success -> temp.value
            is Failure -> return@withContext Failure<List<SleepSession>>(temp)
        }

        Success(response.sessions.filter { it.activity == FitnessActivities.SLEEP }.map { session ->
            val sleepEntriesForSession = arrayListOf<SleepEntry>()
            response.getDataSet(session).forEach { dataSet ->
                sleepEntriesForSession.addAll(dataSet.dataPoints.map { SleepEntry(it.getStartTime(TimeUnit.MILLISECONDS),
                    it.getEndTime(TimeUnit.MILLISECONDS), it.getValue(Field.FIELD_ACTIVITY).asActivity()) })
            }

            SleepSession(session.name ?: "", session.getStartTime(TimeUnit.MILLISECONDS),
                session.getEndTime(TimeUnit.MILLISECONDS), sleepEntriesForSession)
        })
    }

    suspend fun readNutritionData(): Result<List<DataSet>> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_NUTRITION)
            .setTimeRange(now - 180L * DateTimeConstants.MILLIS_PER_DAY, now, TimeUnit.MILLISECONDS)
            .build()

        val response = when (val temp = wrapFitRequest { historyClient?.readData(readRequest)?.await() }) {
            is Success -> temp.value
            is Failure -> return@withContext Failure<List<DataSet>>(temp)
        }

        Success(response.dataSets)
    }

    suspend fun insertSleepData(sleepEntries: List<SleepEntry>) = withContext(Dispatchers.IO) {
        val dataSource: DataSource = DataSource.Builder()
            .setType(DataSource.TYPE_RAW)
            .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
            .setAppPackageName(activity!!)
            .build()

        var dataSet = DataSet.builder(dataSource)
        sleepEntries.forEach {
            dataSet = dataSet.add(
                DataPoint.builder(dataSource)
                    .setTimeInterval(it.startMillis, it.endMillis, TimeUnit.MILLISECONDS)
                    .setActivityField(Field.FIELD_ACTIVITY, it.sleepStage)
                    .build()
            )
        }

        val session: Session = Session.Builder()
            .setName("Sleep")
            .setStartTime(sleepEntries.first().startMillis, TimeUnit.MILLISECONDS) // From first segment
            .setEndTime(sleepEntries.last().endMillis, TimeUnit.MILLISECONDS) // From last segment
            .setActivity(FitnessActivities.SLEEP)
            .build()

        val request = SessionInsertRequest.Builder()
            .setSession(session)
            .addDataSet(dataSet.build())
            .build()


        when (val temp = wrapFitRequest { sessionClient?.insertSession(request)?.await() }) {
            is Failure -> temp.print()
        }
    }

    suspend fun insertSampleNutritionData(): DataSet = withContext(Dispatchers.IO) {
        val dataSource: DataSource = DataSource.Builder()
            .setType(DataSource.TYPE_RAW)
            .setDataType(DataType.TYPE_NUTRITION)
            .setAppPackageName(activity!!)
            .build()

        val nutrients: MutableMap<String, Float> = HashMap()
        nutrients[Field.NUTRIENT_TOTAL_FAT] = 0.4f
        nutrients[Field.NUTRIENT_SODIUM] = 1f
        nutrients[Field.NUTRIENT_SATURATED_FAT] = 0.1f
        nutrients[Field.NUTRIENT_PROTEIN] = 1.3f
        nutrients[Field.NUTRIENT_TOTAL_CARBS] = 27.0f
        nutrients[Field.NUTRIENT_CHOLESTEROL] = 0.0f
        nutrients[Field.NUTRIENT_CALORIES] = 105.0f
        nutrients[Field.NUTRIENT_SUGAR] = 14.0f
        nutrients[Field.NUTRIENT_DIETARY_FIBER] = 3.1f
        nutrients[Field.NUTRIENT_POTASSIUM] = 422f

        val banana = DataPoint.builder(dataSource)
            .setTimestamp(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setField(Field.FIELD_FOOD_ITEM, "banana")
            .setField(Field.FIELD_MEAL_TYPE, Field.MEAL_TYPE_SNACK)
            .setField(Field.FIELD_NUTRIENTS, nutrients)
            .build()

        val dataSet = DataSet.builder(dataSource).add(banana).build()

        when (val temp = wrapFitRequest { historyClient?.insertData(dataSet)?.await() }) {
            is Failure -> temp.print()
        }

        dataSet
    }

    private suspend fun <T> wrapFitRequest(fitAction: suspend () -> Result<T>?): Result<T> = when (val fitActionResult = fitAction.invoke()) {
        is Failure -> when (fitActionResult.error) {
            ErrorCode.AdditionalPermissionsRequired -> activity?.run {
                when (val resolutionResult = this.requestPermissions(listOf(Manifest.permission.ACTIVITY_RECOGNITION)).await()) {
                    is Success -> wrapFitRequest(fitAction)
                    is Failure -> Failure(resolutionResult)
                }
            } ?: Failure(ErrorCode.FailedToRequestPermissions)
            ErrorCode.SignInRequired -> {
                when (val signInResult = signIn()) {
                    is Failure -> Failure(signInResult)
                    is Success -> wrapFitRequest(fitAction)
                }
            }
            ErrorCode.ResolvableIssueOccurred -> activity?.run {
                when (val resolutionResult = fitRequestPermissions()) {
                    is Success -> wrapFitRequest(fitAction)
                    is Failure -> Failure(resolutionResult)
                }
            } ?: Failure(ErrorCode.FailedToRequestPermissions)
            else -> Failure(fitActionResult)
        }
        is Success -> Success(fitActionResult.value)
        else -> when (val signInResult = signIn()) {
            is Failure -> Failure(signInResult)
            is Success -> wrapFitRequest(fitAction)
        }
    }

    private suspend fun signIn(): Result<GoogleSignInAccount> = activity?.run {
        val authResult = this.launchIntentForResult(BaseActivity.RequestCode.RequestSignIn, client.signInIntent)

        when (val temp = authResult.await()) {
            is Failure -> Failure<GoogleSignInAccount>(temp)
            is Success -> Success(temp.value.getParcelableExtra("googleSignInAccount") as GoogleSignInAccount)
        }
    } ?: Failure(ErrorCode.FailedToRequestPermissions)

    private suspend fun fitRequestPermissions(): Result<GoogleSignInAccount> = activity?.run {
        val authResult = this.launchForResult(BaseActivity.RequestCode.RequestOAuthPermission) {
            GoogleSignIn.requestPermissions(it, BaseActivity.RequestCode.RequestOAuthPermission.code,
                googleAccount, fitScope)
        }

        when (val temp = authResult.await()) {
            is Failure -> Failure<GoogleSignInAccount>(temp)
            is Success -> Success(temp.value.getParcelableExtra("googleSignInAccount") as GoogleSignInAccount)
        }
    } ?: Failure(ErrorCode.FailedToRequestPermissions)
}