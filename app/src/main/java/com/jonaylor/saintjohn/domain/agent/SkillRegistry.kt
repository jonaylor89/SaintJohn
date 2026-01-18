package com.jonaylor.saintjohn.domain.agent

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillRegistry @Inject constructor(
    private val skills: Set<@JvmSuppressWildcards AgentSkill>
) {
    fun getSkill(name: String): AgentSkill? = skills.find { it.name == name }
    
    fun getAllSkills(): List<AgentSkill> = skills.toList()
}
