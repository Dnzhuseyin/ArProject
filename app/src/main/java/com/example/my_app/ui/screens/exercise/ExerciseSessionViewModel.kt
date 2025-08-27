package com.example.my_app.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.model.Exercise
import com.example.my_app.data.model.ExerciseSession
import com.example.my_app.data.repository.ExerciseRepository
import com.example.my_app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ExerciseSessionUiState(
    val exercise: Exercise? = null,
    val sessionId: String = "",
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
    val isResting: Boolean = false,
    
    // Progress tracking
    val currentSet: Int = 0,
    val currentReps: Int = 0,
    val totalSets: Int = 0,
    val targetReps: Int = 0,
    
    // Timer
    val elapsedTime: Long = 0L,
    val restTime: Long = 0L,
    val timerText: String = "00:00",
    
    // Sensor data
    val sensorData: Map<String, Float> = emptyMap(),
    val movementCount: Int = 0,
    val accuracy: Float = 0f,
    val caloriesBurned: Int = 0,
    
    // Points and rewards
    val pointsEarned: Int = 0,
    
    val error: String? = null
)

@HiltViewModel
class ExerciseSessionViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExerciseSessionUiState())
    val uiState: StateFlow<ExerciseSessionUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var restTimerJob: Job? = null
    private var sessionStartTime: Long = 0L
    private var exerciseSession: ExerciseSession? = null
    
    fun startExerciseSession(exerciseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val exercise = exerciseRepository.getExerciseById(exerciseId)
                if (exercise != null) {
                    sessionStartTime = System.currentTimeMillis()
                    val sessionId = UUID.randomUUID().toString()
                    
                    exerciseSession = ExerciseSession(
                        id = sessionId,
                        userId = "", // TODO: Get current user ID
                        exerciseId = exerciseId,
                        startTime = Date(sessionStartTime)
                    )
                    
                    // Start the session in repository
                    exerciseRepository.startExerciseSession(exerciseSession!!)
                    
                    _uiState.update { 
                        it.copy(
                            exercise = exercise,
                            sessionId = sessionId,
                            isLoading = false,
                            totalSets = exercise.sets,
                            targetReps = exercise.repetitions,
                            error = null
                        )
                    }
                    
                    startTimer()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Egzersiz bulunamadı"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Egzersiz başlatılırken hata oluştu"
                    )
                }
            }
        }
    }
    
    fun startSet() {
        val currentState = _uiState.value
        if (currentState.currentSet < currentState.totalSets) {
            _uiState.update { 
                it.copy(
                    currentSet = it.currentSet + 1,
                    currentReps = 0,
                    isResting = false
                )
            }
            startTimer()
        }
    }
    
    fun completeSet() {
        val currentState = _uiState.value
        
        // Calculate points for this set
        val setPoints = calculateSetPoints(currentState)
        
        _uiState.update { state ->
            state.copy(
                isResting = true,
                pointsEarned = state.pointsEarned + setPoints
            )
        }
        
        // Start rest timer if not the last set
        if (currentState.currentSet < currentState.totalSets) {
            startRestTimer()
        }
        
        // Update session progress
        updateSessionProgress()
    }
    
    fun completeSession() {
        viewModelScope.launch {
            stopTimer()
            
            val currentState = _uiState.value
            val endTime = System.currentTimeMillis()
            val duration = (endTime - sessionStartTime) / 1000
            
            // Calculate final points
            val totalPoints = calculateTotalPoints(currentState)
            
            // Update exercise session
            exerciseSession?.let { session ->
                val completedSession = session.copy(
                    endTime = Date(endTime),
                    duration = duration,
                    completed = true,
                    completedSets = currentState.currentSet,
                    completedReps = currentState.currentReps,
                    pointsEarned = totalPoints,
                    caloriesBurned = currentState.caloriesBurned
                )
                
                exerciseRepository.completeExerciseSession(completedSession)
            }
            
            _uiState.update { 
                it.copy(
                    isCompleted = true,
                    pointsEarned = totalPoints
                )
            }
        }
    }
    
    fun pauseSession() {
        stopTimer()
        _uiState.update { it.copy(isPaused = true) }
    }
    
    fun resumeSession() {
        _uiState.update { it.copy(isPaused = false) }
        if (!_uiState.value.isResting) {
            startTimer()
        } else {
            startRestTimer()
        }
    }
    
    fun updateCurrentReps(reps: Int) {
        _uiState.update { it.copy(currentReps = maxOf(0, reps)) }
        
        // Simple movement tracking simulation
        if (reps > _uiState.value.currentReps) {
            _uiState.update { 
                it.copy(
                    movementCount = it.movementCount + 1,
                    caloriesBurned = it.caloriesBurned + 1 // Simple calorie calculation
                )
            }
        }
    }
    
    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (!_uiState.value.isPaused && !_uiState.value.isCompleted) {
                delay(1000)
                _uiState.update { state ->
                    val newElapsedTime = state.elapsedTime + 1
                    state.copy(
                        elapsedTime = newElapsedTime,
                        timerText = formatTime(newElapsedTime)
                    )
                }
            }
        }
    }
    
    private fun startRestTimer() {
        val restDuration = _uiState.value.exercise?.restTime ?: 30
        restTimerJob = viewModelScope.launch {
            var remainingTime = restDuration.toLong()
            
            while (remainingTime > 0 && _uiState.value.isResting && !_uiState.value.isPaused) {
                _uiState.update { 
                    it.copy(
                        restTime = remainingTime,
                        timerText = "Dinlenme: ${formatTime(remainingTime)}"
                    )
                }
                delay(1000)
                remainingTime--
            }
            
            if (remainingTime <= 0) {
                _uiState.update { 
                    it.copy(
                        isResting = false,
                        restTime = 0L,
                        timerText = formatTime(it.elapsedTime)
                    )
                }
            }
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
    
    private fun updateSessionProgress() {
        // Simulate sensor data updates
        val accuracy = calculateAccuracy()
        _uiState.update { 
            it.copy(
                accuracy = accuracy,
                sensorData = mapOf(
                    "accelerometer_x" to (Math.random() * 2 - 1).toFloat(),
                    "accelerometer_y" to (Math.random() * 2 - 1).toFloat(),
                    "accelerometer_z" to (Math.random() * 2 - 1).toFloat()
                )
            )
        }
    }
    
    private fun calculateSetPoints(state: ExerciseSessionUiState): Int {
        val basePoints = state.exercise?.points ?: 10
        val completionRatio = if (state.targetReps > 0) {
            state.currentReps.toFloat() / state.targetReps
        } else 1f
        
        return (basePoints * completionRatio * 0.5f).toInt() // Points per set
    }
    
    private fun calculateTotalPoints(state: ExerciseSessionUiState): Int {
        val basePoints = state.exercise?.points ?: 10
        val setCompletionRatio = if (state.totalSets > 0) {
            state.currentSet.toFloat() / state.totalSets
        } else 1f
        
        val accuracyBonus = (state.accuracy * 0.2f).toInt()
        
        return (basePoints * setCompletionRatio).toInt() + accuracyBonus
    }
    
    private fun calculateAccuracy(): Float {
        // Simulate accuracy calculation based on sensor data
        // In a real app, this would analyze actual sensor readings
        val currentState = _uiState.value
        val targetReps = currentState.targetReps
        val actualReps = currentState.currentReps
        
        return if (targetReps > 0) {
            minOf(1f, actualReps.toFloat() / targetReps)
        } else 1f
    }
    
    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}