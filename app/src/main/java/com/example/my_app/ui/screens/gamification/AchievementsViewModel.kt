package com.example.my_app.ui.screens.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.model.Achievement
import com.example.my_app.data.model.AchievementCategory
import com.example.my_app.data.repository.GamificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val filteredAchievements: List<Achievement> = emptyList(),
    val selectedCategory: AchievementCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val gamificationRepository: GamificationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()
    
    fun loadAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                gamificationRepository.getAllAchievements().collect { achievements ->
                    val filteredAchievements = filterAchievements(achievements, _uiState.value.selectedCategory)
                    
                    _uiState.update { 
                        it.copy(
                            achievements = achievements,
                            filteredAchievements = filteredAchievements,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Başarımlar yüklenirken hata oluştu"
                    )
                }
            }
        }
    }
    
    fun selectCategory(category: AchievementCategory?) {
        val currentState = _uiState.value
        val newCategory = if (currentState.selectedCategory == category) null else category
        val filteredAchievements = filterAchievements(currentState.achievements, newCategory)
        
        _uiState.update { 
            it.copy(
                selectedCategory = newCategory,
                filteredAchievements = filteredAchievements
            )
        }
    }
    
    private fun filterAchievements(
        achievements: List<Achievement>, 
        category: AchievementCategory?
    ): List<Achievement> {
        return if (category == null) {
            achievements.sortedWith(
                compareByDescending<Achievement> { it.isUnlocked }
                    .thenBy { it.category }
                    .thenBy { it.title }
            )
        } else {
            achievements.filter { it.category == category }
                .sortedWith(
                    compareByDescending<Achievement> { it.isUnlocked }
                        .thenBy { it.title }
                )
        }
    }
}