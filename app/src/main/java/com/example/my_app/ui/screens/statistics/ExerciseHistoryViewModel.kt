package com.example.my_app.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.model.ExerciseSession
import com.example.my_app.data.repository.ExerciseRepository
import com.example.my_app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ExerciseHistoryUiState(
    val sessions: List<ExerciseSession> = emptyList(),
    val filteredSessions: List<ExerciseSession> = emptyList(),
    val selectedFilter: String = "Tümü",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExerciseHistoryViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExerciseHistoryUiState())
    val uiState: StateFlow<ExerciseHistoryUiState> = _uiState.asStateFlow()
    
    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                userRepository.getCurrentUser().collect { user ->
                    if (user != null) {
                        exerciseRepository.getUserSessions(user.id).collect { sessions ->
                            val sortedSessions = sessions.sortedByDescending { it.startTime }
                            val filteredSessions = filterSessions(sortedSessions, _uiState.value.selectedFilter)
                            
                            _uiState.update { 
                                it.copy(
                                    sessions = sortedSessions,
                                    filteredSessions = filteredSessions,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Kullanıcı bulunamadı"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Geçmiş yüklenirken hata oluştu"
                    )
                }
            }
        }
    }
    
    fun selectFilter(filter: String) {
        val currentState = _uiState.value
        val filteredSessions = filterSessions(currentState.sessions, filter)
        
        _uiState.update { 
            it.copy(
                selectedFilter = filter,
                filteredSessions = filteredSessions
            )
        }
    }
    
    fun refreshHistory() {
        loadHistory()
    }
    
    private fun filterSessions(sessions: List<ExerciseSession>, filter: String): List<ExerciseSession> {
        val calendar = Calendar.getInstance()
        val now = calendar.time
        
        return when (filter) {
            "Tümü" -> sessions
            "Tamamlanan" -> sessions.filter { it.completed }
            "Devam Eden" -> sessions.filter { !it.completed }
            "Bu Hafta" -> {
                calendar.add(Calendar.DAY_OF_WEEK, -7)
                val weekAgo = calendar.time
                sessions.filter { it.startTime.after(weekAgo) }
            }
            "Bu Ay" -> {
                calendar.time = now
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.time
                sessions.filter { it.startTime.after(monthAgo) }
            }
            else -> sessions
        }
    }
}