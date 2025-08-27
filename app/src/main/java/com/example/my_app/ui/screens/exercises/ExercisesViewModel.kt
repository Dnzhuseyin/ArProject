package com.example.my_app.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.model.Exercise
import com.example.my_app.data.model.ExerciseCategory
import com.example.my_app.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExercisesUiState(
    val exercises: List<Exercise> = emptyList(),
    val selectedCategory: ExerciseCategory? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExercisesUiState())
    val uiState: StateFlow<ExercisesUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<ExerciseCategory?>(null)
    
    init {
        loadExercises()
    }
    
    private fun loadExercises() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                combine(
                    exerciseRepository.getAssignedExercises(),
                    _selectedCategory,
                    _searchQuery
                ) { exercises, category, query ->
                    var filteredExercises = exercises
                    
                    // Filter by category
                    category?.let { cat ->
                        filteredExercises = filteredExercises.filter { it.category == cat }
                    }
                    
                    // Filter by search query
                    if (query.isNotBlank()) {
                        filteredExercises = filteredExercises.filter { exercise ->
                            exercise.title.contains(query, ignoreCase = true) ||
                            exercise.description.contains(query, ignoreCase = true)
                        }
                    }
                    
                    _uiState.update {
                        it.copy(
                            exercises = filteredExercises,
                            selectedCategory = category,
                            searchQuery = query,
                            isLoading = false,
                            error = null
                        )
                    }
                }.collect()
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun selectCategory(category: ExerciseCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun refreshExercises() {
        viewModelScope.launch {
            exerciseRepository.syncExercises()
        }
    }
}