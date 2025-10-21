package com.jonaylor.saintjohn.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonaylor.saintjohn.domain.model.Note
import com.jonaylor.saintjohn.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _editingNote = MutableStateFlow<Note?>(null)
    val editingNote = _editingNote.asStateFlow()

    val uiState: StateFlow<NotesUiState> = noteRepository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ).let { notesFlow ->
            MutableStateFlow(NotesUiState()).also { state ->
                viewModelScope.launch {
                    notesFlow.collect { notes ->
                        state.value = NotesUiState(
                            notes = notes,
                            isLoading = false
                        )
                    }
                }
            }
        }

    fun addNote(title: String, content: String = "") {
        if (title.isBlank() && content.isBlank()) return

        viewModelScope.launch {
            val note = Note(
                title = title.trim(),
                content = content.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            noteRepository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(
                note.copy(updatedAt = System.currentTimeMillis())
            )
            _editingNote.value = null
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun startEditingNote(note: Note) {
        _editingNote.value = note
    }

    fun cancelEditing() {
        _editingNote.value = null
    }
}
