package com.jonaylor.saintjohn.domain.agent.skills

import com.jonaylor.saintjohn.domain.agent.AgentSkill
import com.jonaylor.saintjohn.domain.agent.SkillParameter
import com.jonaylor.saintjohn.domain.repository.AppRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ListAppsSkill @Inject constructor(
    private val appRepository: AppRepository
) : AgentSkill(
    name = "list_apps",
    description = "Lists all installed applications on the device.",
    params = emptyList()
) {
    override suspend fun execute(args: Map<String, Any?>): String {
        val apps = appRepository.getAllApps().first()
        if (apps.isEmpty()) return "No apps found."
        
        return apps.joinToString("\n") { "${it.label} (${it.packageName})" }
    }
}
