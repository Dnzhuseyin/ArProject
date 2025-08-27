package com.example.my_app.ui.screens.gamification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.my_app.data.model.Achievement
import com.example.my_app.data.model.AchievementCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    navController: NavController,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadAchievements()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Başarımlar") },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri"
                    )
                }
            }
        )
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AchievementsHeader(
                            totalAchievements = uiState.achievements.size,
                            unlockedAchievements = uiState.achievements.count { it.isUnlocked },
                            totalPoints = uiState.achievements.filter { it.isUnlocked }.sumOf { it.points }
                        )
                    }
                    
                    item {
                        CategoryFilter(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = viewModel::selectCategory
                        )
                    }
                    
                    items(uiState.filteredAchievements) { achievement ->
                        AchievementCard(achievement = achievement)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsHeader(
    totalAchievements: Int,
    unlockedAchievements: Int,
    totalPoints: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Başarım Koleksiyonunuz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AchievementStatItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Başarım",
                    value = "$unlockedAchievements/$totalAchievements"
                )
                
                AchievementStatItem(
                    icon = Icons.Default.Star,
                    label = "Toplam Puan",
                    value = "$totalPoints"
                )
                
                AchievementStatItem(
                    icon = Icons.Default.Percent,
                    label = "Tamamlanma",
                    value = "${if (totalAchievements > 0) (unlockedAchievements * 100 / totalAchievements) else 0}%"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = if (totalAchievements > 0) unlockedAchievements.toFloat() / totalAchievements else 0f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AchievementStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun CategoryFilter(
    selectedCategory: AchievementCategory?,
    onCategorySelected: (AchievementCategory?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                onClick = { onCategorySelected(null) },
                label = { Text("Tümü") },
                selected = selectedCategory == null
            )
        }
        
        items(AchievementCategory.values()) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(getCategoryName(category)) },
                selected = selectedCategory == category
            )
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (achievement.isUnlocked) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Achievement icon
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Achievement details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (achievement.isUnlocked) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar for locked achievements
                if (!achievement.isUnlocked) {
                    LinearProgressIndicator(
                        progress = if (achievement.requiredValue > 0) {
                            achievement.progress.toFloat() / achievement.requiredValue
                        } else 0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${achievement.progress}/${achievement.requiredValue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                // Unlock date for completed achievements
                achievement.unlockedAt?.let { date ->
                    Text(
                        text = "Açıldı: ${android.text.format.DateFormat.getDateInstance().format(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Points
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Points",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${achievement.points}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun getCategoryName(category: AchievementCategory): String {
    return when (category) {
        AchievementCategory.GENERAL -> "Genel"
        AchievementCategory.STREAK -> "Seri"
        AchievementCategory.EXERCISES -> "Egzersiz"
        AchievementCategory.POINTS -> "Puan"
        AchievementCategory.CONSISTENCY -> "Tutarlılık"
        AchievementCategory.MILESTONES -> "Kilometre Taşları"
    }
}