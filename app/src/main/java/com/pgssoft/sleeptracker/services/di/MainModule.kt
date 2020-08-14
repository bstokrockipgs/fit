package com.pgssoft.sleeptracker.services.di

import com.pgssoft.sleeptracker.services.FitService
import com.pgssoft.sleeptracker.services.PermissionsService
import org.koin.dsl.module

val mainModule = module {
    single { FitService(get(), get()) }
    single { PermissionsService(get()) }
}