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

data class StatisticsUiState(
    val totalExercises: Int = 0,
    val totalExerciseTime: Long = 0,
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val weeklyProgress: List<Pair<String, Int>> = emptyList(),
    val exercisesByCategory: List<Pair<String, Int>> = emptyList(),
    val recentSessions: List<ExerciseSession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Get current user for user ID
                userRepository.getCurrentUser().collect { user ->
                    if (user != null) {
                        val userId = user.id
                        
                        // Load all statistics concurrently
                        combine(
                            exerciseRepository.getUserSessions(userId),
                            exerciseRepository.getCompletedSessions(userId)
                        ) { allSessions, completedSessions ->
                            val (totalExercises, totalTime, totalPoints) = exerciseRepository.getUserStats(userId)
                            
                            _uiState.update { state ->
                                state.copy(
                                    totalExercises = totalExercises,
                                    totalExerciseTime = totalTime,
                                    totalPoints = totalPoints,
                                    currentStreak = user.streakDays,
                                    weeklyProgress = calculateWeeklyProgress(completedSessions),
                                    exercisesByCategory = calculateCategoryStats(completedSessions),
                                    recentSessions = completedSessions.take(10),
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }.collect()
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
                        error = e.message ?: "İstatistikler yüklenirken hata oluştu"
                    )
                }
            }
        }
    }
    
    fun refreshStatistics() {
        loadStatistics()
    }
    
    private fun calculateWeeklyProgress(sessions: List<ExerciseSession>): List<Pair<String, Int>> {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        // Get last 7 days
        val weeklyData = mutableListOf<Pair<String, Int>>()\n        \n        for (i in 6 downTo 0) {\n            calendar.time = today\n            calendar.add(Calendar.DAY_OF_MONTH, -i)\n            \n            val dayName = when (calendar.get(Calendar.DAY_OF_WEEK)) {\n                Calendar.MONDAY -> \"Pzt\"\n                Calendar.TUESDAY -> \"Sal\"\n                Calendar.WEDNESDAY -> \"Çar\"\n                Calendar.THURSDAY -> \"Per\"\n                Calendar.FRIDAY -> \"Cum\"\n                Calendar.SATURDAY -> \"Cmt\"\n                Calendar.SUNDAY -> \"Paz\"\n                else -> \"?\"\n            }\n            \n            val dayStart = calendar.apply {\n                set(Calendar.HOUR_OF_DAY, 0)\n                set(Calendar.MINUTE, 0)\n                set(Calendar.SECOND, 0)\n                set(Calendar.MILLISECOND, 0)\n            }.time\n            \n            val dayEnd = calendar.apply {\n                add(Calendar.DAY_OF_MONTH, 1)\n                add(Calendar.MILLISECOND, -1)\n            }.time\n            \n            val dayCount = sessions.count { session ->\n                session.startTime.after(dayStart) && session.startTime.before(dayEnd)\n            }\n            \n            weeklyData.add(dayName to dayCount)\n        }\n        \n        return weeklyData\n    }\n    \n    private fun calculateCategoryStats(sessions: List<ExerciseSession>): List<Pair<String, Int>> {\n        // This is a simplified version - in a real app, you'd join with exercise data\n        // to get actual categories\n        return listOf(\n            \"Genel\" to sessions.count(),\n            \"Omuz\" to (sessions.count() * 0.3).toInt(),\n            \"Diz\" to (sessions.count() * 0.2).toInt(),\n            \"Sırt\" to (sessions.count() * 0.25).toInt(),\n            \"Diğer\" to (sessions.count() * 0.25).toInt()\n        ).filter { it.second > 0 }\n    }\n}