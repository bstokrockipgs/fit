package com.pgssoft.sleeptracker.ui.sleep

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.pgssoft.sleeptracker.errorhandling.Failure
import com.pgssoft.sleeptracker.errorhandling.Success
import com.pgssoft.sleeptracker.services.FitService
import com.pgssoft.sleeptracker.ui.BaseViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.DateTimeConstants

class SleepViewModel(private val fitService: FitService) : BaseViewModel() {
    val sleepActivities = MutableLiveData<List<SleepActivityViewModel>>().apply { value = emptyList() }

    suspend fun loadSleepDataFromFit() {
        when (val temp = fitService.readSleepData()) {
            is Success -> {
                sleepActivities.postValue(temp.value.map { SleepActivityViewModel(it.start, it.end, true) })
            }
            is Failure -> {
                temp.print()
            }
        }
    }

    fun addButtonClicked() {
        val earliestItem = sleepActivities.value?.minBy { it.startTime }
        val newStartTime = (earliestItem?.startTime ?: System.currentTimeMillis()) - DateTimeConstants.MILLIS_PER_DAY
        val newEndTime = newStartTime + DateTimeConstants.MILLIS_PER_HOUR * 6

        val updatedList = (sleepActivities.value ?: emptyList()) + SleepActivityViewModel(newStartTime, newEndTime, false)
        sleepActivities.postValue(updatedList)

        GlobalScope.launch { fitService.insertSleepData(updatedList.last().sleepEntries) }
    }

    fun syncButtonClicked() {
        GlobalScope.launch { loadSleepDataFromFit() }
    }
}