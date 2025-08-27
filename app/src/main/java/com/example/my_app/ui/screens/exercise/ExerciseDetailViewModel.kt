package com.example.my_app.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.model.Exercise
import com.example.my_app.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseDetailUiState(
    val exercise: Exercise? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false
)

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExerciseDetailUiState())
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()
    
    fun loadExercise(exerciseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val exercise = exerciseRepository.getExerciseById(exerciseId)
                if (exercise != null) {
                    _uiState.update { 
                        it.copy(
                            exercise = exercise,
                            isLoading = false,
                            error = null
                        )
                    }
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
                        error = e.message ?: "Egzersiz yüklenirken hata oluştu"
                    )
                }
            }
        }
    }
    
    fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        // TODO: Implement favorite functionality
    }
}