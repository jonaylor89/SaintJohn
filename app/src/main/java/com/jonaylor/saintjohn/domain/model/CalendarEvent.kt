package com.jonaylor.saintjohn.domain.model

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val location: String? = null,
    val allDay: Boolean = false
)
