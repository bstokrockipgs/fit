package com.pgssoft.sleeptracker.ui.nutrition

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.fitness.FitnessActivities
import com.pgssoft.sleeptracker.model.NutritionEntry
import com.pgssoft.sleeptracker.model.SleepEntry
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.format.DateTimeFormat

class NutritionRecordViewModel(time: Long, fat: Float, sugar: Float, carbs: Float, isSynced: Boolean) {
    private val dateFormat = DateTimeFormat.forPattern("HH:ss, dd MMM")
    val timeString = MutableLiveData<String>().apply { postValue(DateTime(time).toString(dateFormat)) }
    val fat = MutableLiveData<String>().apply { postValue("Fat: %.2f g".format(fat)) }
    val sugar = MutableLiveData<String>().apply { postValue("Sugar: %.2f g".format(sugar)) }
    val carbs = MutableLiveData<String>().apply { postValue("Carbs: %.2f g".format(carbs)) }
    val isSynced = MutableLiveData<Boolean>().apply { postValue(isSynced) }
}