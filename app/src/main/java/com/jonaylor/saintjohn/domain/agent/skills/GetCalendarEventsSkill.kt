package com.jonaylor.saintjohn.domain.agent.skills

import com.jonaylor.saintjohn.domain.agent.AgentSkill
import com.jonaylor.saintjohn.domain.agent.SkillParameter
import com.jonaylor.saintjohn.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

class GetCalendarEventsSkill @Inject constructor(
    private val calendarRepository: CalendarRepository
) : AgentSkill(
    name = "get_calendar_events",
    description = "Gets upcoming calendar events.",
    params = listOf(
        SkillParameter(
            name = "limit",
            type = "integer",
            description = "Maximum number of events to retrieve (default is 5)",
            required = false
        )
    )
) {
    override suspend fun execute(args: Map<String, Any?>): String {
        val limit = (args["limit"] as? Number)?.toInt() ?: 5
        val events = calendarRepository.getUpcomingEvents(limit).first()
        
        if (events.isEmpty()) return "No upcoming events found."
        
                val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                return events.joinToString("\n") { event ->
                    "${event.title} at ${sdf.format(Date(event.startTime))}" + 
                    (if (!event.location.isNullOrBlank()) " in ${event.location}" else "")
                }    }
}
