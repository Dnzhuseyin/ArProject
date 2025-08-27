package com.example.my_app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.model.Achievement
import com.example.my_app.data.model.DailyTask
import com.example.my_app.data.model.Exercise
import com.example.my_app.data.repository.ExerciseRepository
import com.example.my_app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val todayCompletedExercises: Int = 0,
    val todayTotalExercises: Int = 0,
    val todayPoints: Int = 0,
    val streakDays: Int = 0,
    val todayExercises: List<Exercise> = emptyList(),
    val dailyTasks: List<DailyTask> = emptyList(),
    val recentAchievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {\n        loadHomeData()\n    }\n    \n    private fun loadHomeData() {\n        viewModelScope.launch {\n            _uiState.update { it.copy(isLoading = true) }\n            \n            try {\n                // Combine all data flows\n                combine(\n                    userRepository.getCurrentUser(),\n                    exerciseRepository.getAssignedExercises(),\n                    // Add other data sources here\n                ) { user, exercises ->\n                    val currentUser = user\n                    val assignedExercises = exercises\n                    \n                    // Get today's completed sessions\n                    val todaySessions = if (currentUser != null) {\n                        exerciseRepository.getTodayCompletedSessions(currentUser.id)\n                    } else {\n                        emptyList()\n                    }\n                    \n                    HomeUiState(\n                        userName = currentUser?.username ?: \"Kullanıcı\",\n                        todayCompletedExercises = todaySessions.size,\n                        todayTotalExercises = assignedExercises.size,\n                        todayPoints = todaySessions.sumOf { it.pointsEarned },\n                        streakDays = currentUser?.streakDays ?: 0,\n                        todayExercises = assignedExercises.take(5), // Show first 5\n                        dailyTasks = generateDailyTasks(),\n                        recentAchievements = generateRecentAchievements(),\n                        isLoading = false\n                    )\n                }.catch { exception ->\n                    _uiState.update { \n                        it.copy(\n                            isLoading = false,\n                            error = exception.message\n                        )\n                    }\n                }.collect { newState ->\n                    _uiState.value = newState\n                }\n                \n            } catch (e: Exception) {\n                _uiState.update { \n                    it.copy(\n                        isLoading = false,\n                        error = e.message\n                    )\n                }\n            }\n        }\n    }\n    \n    fun completeTask(taskId: String) {\n        viewModelScope.launch {\n            // Update task completion logic\n            val currentTasks = _uiState.value.dailyTasks\n            val updatedTasks = currentTasks.map { task ->\n                if (task.id == taskId) {\n                    task.copy(isCompleted = true, currentProgress = task.targetValue)\n                } else {\n                    task\n                }\n            }\n            \n            _uiState.update { \n                it.copy(dailyTasks = updatedTasks)\n            }\n        }\n    }\n    \n    fun refreshData() {\n        loadHomeData()\n    }\n    \n    private fun generateDailyTasks(): List<DailyTask> {\n        // For now, return mock data. In a real app, this would come from repository\n        return listOf(\n            DailyTask(\n                id = \"task1\",\n                title = \"3 Egzersiz Tamamla\",\n                description = \"Bugün en az 3 egzersiz tamamlayın\",\n                targetValue = 3,\n                currentProgress = 1,\n                points = 50,\n                isCompleted = false\n            ),\n            DailyTask(\n                id = \"task2\",\n                title = \"30 Dakika Egzersiz\",\n                description = \"Toplam 30 dakika egzersiz yapın\",\n                targetValue = 30,\n                currentProgress = 15,\n                points = 30,\n                isCompleted = false\n            )\n        )\n    }\n    \n    private fun generateRecentAchievements(): List<Achievement> {\n        // Mock achievements - in real app, this would come from repository\n        return listOf(\n            Achievement(\n                id = \"ach1\",\n                title = \"İlk Egzersiz\",\n                description = \"İlk egzersiznizi tamamladınız\",\n                points = 25,\n                isUnlocked = true\n            ),\n            Achievement(\n                id = \"ach2\",\n                title = \"Haftanın Şampiyonu\",\n                description = \"Bu hafta 7 egzersiz tamamladınız\",\n                points = 100,\n                isUnlocked = true\n            )\n        )\n    }\n}