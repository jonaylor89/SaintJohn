package com.jonaylor.saintjohn.domain.repository

import com.jonaylor.saintjohn.domain.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun getUpcomingEvents(limit: Int = 5): Flow<List<CalendarEvent>>
    suspend fun refreshEvents()
}
