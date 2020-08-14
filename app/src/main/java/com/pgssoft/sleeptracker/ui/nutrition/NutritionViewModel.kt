package com.pgssoft.sleeptracker.ui.nutrition

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.Field
import com.pgssoft.sleeptracker.errorhandling.Failure
import com.pgssoft.sleeptracker.errorhandling.Success
import com.pgssoft.sleeptracker.services.FitService
import com.pgssoft.sleeptracker.ui.BaseViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NutritionViewModel(private val fitService: FitService) : BaseViewModel() {
    val nutritionRecords = MutableLiveData<List<NutritionRecordViewModel>>().apply { value = emptyList() }

    suspend fun loadNutritionDataFromFit() {
        when (val temp = fitService.readNutritionData()) {
            is Success -> {
                temp.value.filter { it.dataPoints.isNotEmpty() }.forEach { dataSet ->
                    nutritionRecords.postValue(dataSet.dataPoints.map { newRecordViewModel(it) })
                }
            }
            is Failure -> {
                temp.print()
            }
        }
    }

    fun addButtonClicked() {
        GlobalScope.launch {
            val dataSet = fitService.insertSampleNutritionData()

            val updatedList = (nutritionRecords.value ?: emptyList()) + dataSet.dataPoints.map { newRecordViewModel(it) }
            nutritionRecords.postValue(updatedList)
        }
    }

    private fun newRecordViewModel(nutritionDataSet: DataPoint): NutritionRecordViewModel {
        val value = nutritionDataSet.getValue(Field.FIELD_NUTRIENTS)

        return NutritionRecordViewModel(
            nutritionDataSet.getTimestamp(TimeUnit.MILLISECONDS),
            value.getKeyValue(Field.NUTRIENT_TOTAL_FAT) ?: 0f,
            value.getKeyValue(Field.NUTRIENT_SUGAR) ?: 0f,
            value.getKeyValue(Field.NUTRIENT_SUGAR) ?: 0f,
            true
        )
    }

    fun syncButtonClicked() {
        GlobalScope.launch { loadNutritionDataFromFit() }
    }
}