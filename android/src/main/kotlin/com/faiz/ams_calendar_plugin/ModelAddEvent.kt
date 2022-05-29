package com.faiz.ams_calendar_plugin

data class ModelAddEvent(
    val eventTitle: String?,
    val eventLocation: String?,
    val eventDescription: String?,
    val beginTime: Long,
    val endTime: Long,
    val reminderInMinutes: Int
)