package com.pgssoft.sleeptracker.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    protected val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast
}