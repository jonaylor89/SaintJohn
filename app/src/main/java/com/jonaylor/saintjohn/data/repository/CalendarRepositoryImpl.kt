package com.jonaylor.saintjohn.data.repository

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.jonaylor.saintjohn.domain.model.CalendarEvent
import com.jonaylor.saintjohn.domain.repository.CalendarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarRepository {

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())

    override fun getUpcomingEvents(limit: Int): Flow<List<CalendarEvent>> {
        return _events.asStateFlow()
    }

    override suspend fun refreshEvents() = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            _events.value = emptyList()
            return@withContext
        }

        try {
            val events = readCalendarEvents()
            _events.value = events
        } catch (e: Exception) {
            e.printStackTrace()
            _events.value = emptyList()
        }
    }

    private fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun readCalendarEvents(): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val contentResolver: ContentResolver = context.contentResolver

        // Get current time
        val now = System.currentTimeMillis()

        // Query for events starting from now
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.CALENDAR_DISPLAY_NAME,
            CalendarContract.Events.CALENDAR_COLOR,
            CalendarContract.Events.ALL_DAY
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ?"
        val selectionArgs = arrayOf(now.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(CalendarContract.Events._ID)
                val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
                val descIndex = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                val startIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val endIndex = it.getColumnIndex(CalendarContract.Events.DTEND)
                val locationIndex = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
                val calendarNameIndex = it.getColumnIndex(CalendarContract.Events.CALENDAR_DISPLAY_NAME)
                val calendarColorIndex = it.getColumnIndex(CalendarContract.Events.CALENDAR_COLOR)
                val allDayIndex = it.getColumnIndex(CalendarContract.Events.ALL_DAY)

                while (it.moveToNext() && events.size < 10) { // Limit to 10 upcoming events
                    val id = if (idIndex != -1) it.getLong(idIndex) else 0L
                    val title = if (titleIndex != -1) it.getString(titleIndex) ?: "Untitled" else "Untitled"
                    val description = if (descIndex != -1) it.getString(descIndex) else null
                    val startTime = if (startIndex != -1) it.getLong(startIndex) else 0L
                    val endTime = if (endIndex != -1) it.getLong(endIndex) else startTime
                    val location = if (locationIndex != -1) it.getString(locationIndex) else null
                    val calendarName = if (calendarNameIndex != -1) it.getString(calendarNameIndex) ?: "" else ""
                    val calendarColor = if (calendarColorIndex != -1) it.getInt(calendarColorIndex) else 0
                    val allDay = if (allDayIndex != -1) it.getInt(allDayIndex) == 1 else false

                    events.add(
                        CalendarEvent(
                            id = id,
                            title = title,
                            description = description,
                            startTime = startTime,
                            endTime = endTime,
                            location = location,
                            calendarName = calendarName,
                            calendarColor = calendarColor,
                            allDay = allDay
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return events
    }
}
