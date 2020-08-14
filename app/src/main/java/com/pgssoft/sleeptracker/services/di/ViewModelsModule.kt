package com.pgssoft.sleeptracker.services.di

import com.pgssoft.sleeptracker.ui.sleep.SleepViewModel
import com.pgssoft.sleeptracker.ui.nutrition.NutritionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { SleepViewModel(get()) }
    viewModel { NutritionViewModel(get()) }
}