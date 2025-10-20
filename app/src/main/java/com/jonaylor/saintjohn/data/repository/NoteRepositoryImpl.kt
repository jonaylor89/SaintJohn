package com.jonaylor.saintjohn.data.repository

import com.jonaylor.saintjohn.data.local.dao.NoteDao
import com.jonaylor.saintjohn.data.local.entity.NoteEntity
import com.jonaylor.saintjohn.domain.model.Note
import com.jonaylor.saintjohn.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)?.toDomainModel()
    }

    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
    }

    private fun NoteEntity.toDomainModel(): Note {
        return Note(
            id = id,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
