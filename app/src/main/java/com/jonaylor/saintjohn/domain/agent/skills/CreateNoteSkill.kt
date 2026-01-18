package com.jonaylor.saintjohn.domain.agent.skills

import com.jonaylor.saintjohn.domain.agent.AgentSkill
import com.jonaylor.saintjohn.domain.agent.SkillParameter
import com.jonaylor.saintjohn.domain.model.Note
import com.jonaylor.saintjohn.domain.repository.NoteRepository
import javax.inject.Inject

class CreateNoteSkill @Inject constructor(
    private val noteRepository: NoteRepository
) : AgentSkill(
    name = "create_note",
    description = "Creates a new quick note.",
    params = listOf(
        SkillParameter(
            name = "title",
            type = "string",
            description = "The title of the note",
            required = true
        ),
        SkillParameter(
            name = "content",
            type = "string",
            description = "The content of the note",
            required = true
        )
    )
) {
    override suspend fun execute(args: Map<String, Any?>): String {
        val title = args["title"] as? String ?: return "Error: Title is required."
        val content = args["content"] as? String ?: return "Error: Content is required."
        
        val note = Note(
            title = title,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        noteRepository.insertNote(note)
        return "Note created successfully: $title"
    }
}
