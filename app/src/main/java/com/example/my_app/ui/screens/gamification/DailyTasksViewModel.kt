package com.example.my_app.ui.screens.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.model.DailyTask
import com.example.my_app.data.repository.GamificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DailyTasksUiState(
    val tasks: List<DailyTask> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DailyTasksViewModel @Inject constructor(
    private val gamificationRepository: GamificationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DailyTasksUiState())
    val uiState: StateFlow<DailyTasksUiState> = _uiState.asStateFlow()
    
    fun loadDailyTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                gamificationRepository.getDailyTasks(Date()).collect { tasks ->
                    _uiState.update { 
                        it.copy(
                            tasks = tasks,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Görevler yüklenirken hata oluştu"
                    )
                }
            }
        }
    }
    
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            gamificationRepository.completeTask(taskId).fold(
                onSuccess = {
                    // Task completion handled in repository
                    // UI will update automatically through the flow
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message ?: "Görev tamamlanırken hata oluştu")
                    }
                }
            )
        }
    }
    
    fun generateTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // TODO: Get current user ID
            val userId = "current_user_id"
            
            gamificationRepository.generateDailyTasks(userId, Date()).fold(
                onSuccess = { tasks ->
                    _uiState.update { 
                        it.copy(
                            tasks = tasks,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Görevler oluşturulurken hata oluştu"
                        )
                    }
                }
            )
        }
    }
    
    fun refreshTasks() {
        loadDailyTasks()
    }
}