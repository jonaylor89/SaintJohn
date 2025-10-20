package com.jonaylor.saintjohn.domain.model

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String? = null,
    val startTime: Long,
    val endTime: Long,
    val location: String? = null,
    val calendarName: String = "",
    val calendarColor: Int = 0,
    val allDay: Boolean = false
)
