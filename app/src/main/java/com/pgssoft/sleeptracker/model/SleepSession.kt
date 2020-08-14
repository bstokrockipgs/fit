package com.pgssoft.sleeptracker.model

data class SleepSession(val name: String, val start: Long, val end: Long, val entries: List<SleepEntry>)