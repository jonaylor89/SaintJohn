package com.jonaylor.saintjohn.presentation.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonaylor.saintjohn.domain.model.AppCategory
import com.jonaylor.saintjohn.domain.model.AppInfo
import com.jonaylor.saintjohn.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DrawerUiState(
    val categorizedApps: Map<AppCategory, List<AppInfo>> = emptyMap(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val filteredApps: List<AppInfo> = emptyList()
)

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val uiState: StateFlow<DrawerUiState> = combine(
        appRepository.getAllApps(),
        _searchQuery
    ) { apps, query ->
        try {
            val categorized = apps.groupBy { it.category }
            val filtered = if (query.isBlank()) {
                emptyList()
            } else {
                apps.filter {
                    it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
                }
            }

            DrawerUiState(
                categorizedApps = categorized,
                isLoading = false,
                searchQuery = query,
                filteredApps = filtered
            )
        } catch (e: Exception) {
            e.printStackTrace()
            DrawerUiState(
                categorizedApps = emptyMap(),
                isLoading = false,
                searchQuery = query,
                filteredApps = emptyList()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DrawerUiState(isLoading = true)
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun launchApp(packageName: String) {
        // Will be handled by the Activity
    }

    fun updateAppVisibility(packageName: String, isHidden: Boolean) {
        viewModelScope.launch {
            appRepository.updateAppVisibility(packageName, isHidden)
        }
    }

    fun updateAppLockStatus(packageName: String, isLocked: Boolean) {
        viewModelScope.launch {
            appRepository.updateAppLockStatus(packageName, isLocked)
        }
    }

    fun updateAppColorMode(packageName: String, forceColor: Boolean) {
        viewModelScope.launch {
            appRepository.updateAppColorMode(packageName, forceColor)
        }
    }

    fun updateAppCategory(packageName: String, category: AppCategory) {
        viewModelScope.launch {
            appRepository.updateAppCategory(packageName, category)
        }
    }
}
