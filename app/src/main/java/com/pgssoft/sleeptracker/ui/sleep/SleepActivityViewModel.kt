package com.pgssoft.sleeptracker.ui.sleep

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.fitness.FitnessActivities
import com.pgssoft.sleeptracker.model.SleepEntry
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.format.DateTimeFormat

class SleepActivityViewModel(val startTime: Long, val endTime: Long, isSynced: Boolean) {
    private val dateFormat = DateTimeFormat.forPattern("HH:ss, dd MMM")
    val startTimeString = MutableLiveData<String>().apply { postValue(DateTime(startTime).toString(dateFormat)) }
    val endTimeString = MutableLiveData<String>().apply { postValue(DateTime(endTime).toString(dateFormat)) }
    val isSynced = MutableLiveData<Boolean>().apply { postValue(isSynced) }

    val sleepEntries = arrayListOf<SleepEntry>().apply {
        val sleepStages = arrayOf(FitnessActivities.SLEEP_AWAKE, FitnessActivities.SLEEP_LIGHT, FitnessActivities.SLEEP_DEEP, FitnessActivities.SLEEP_REM)
        val halfHourMillis = 30 * DateTimeConstants.MILLIS_PER_MINUTE
        val entriesCount = ((endTime - startTime) / halfHourMillis).toInt()
        for (i in 0 until entriesCount) {
            add(SleepEntry(startTime + i * halfHourMillis, startTime + (i + 1) * halfHourMillis, sleepStages[i % sleepStages.size]))
        }
    }
}