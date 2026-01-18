package com.jonaylor.saintjohn.domain.agent

data class SkillParameter(
    val name: String,
    val type: String, // "string", "integer", "boolean", "number"
    val description: String,
    val required: Boolean = true,
    val enumValues: List<String>? = null
)

abstract class AgentSkill(
    val name: String,
    val description: String,
    val params: List<SkillParameter> = emptyList()
) {
    abstract suspend fun execute(args: Map<String, Any?>): String
}
