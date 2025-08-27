package com.example.my_app.ui.screens.statistics

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.my_app.data.model.ExerciseSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("İstatistikler") },
            actions = {
                IconButton(
                    onClick = { viewModel.refreshStatistics() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yenile"
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
                        StatisticsOverview(
                            totalExercises = uiState.totalExercises,
                            totalTime = uiState.totalExerciseTime,
                            totalPoints = uiState.totalPoints,
                            currentStreak = uiState.currentStreak
                        )
                    }
                    
                    item {
                        WeeklyProgressCard(
                            weeklyData = uiState.weeklyProgress
                        )
                    }
                    
                    item {
                        ExerciseCategoryChart(
                            categoryData = uiState.exercisesByCategory
                        )
                    }
                    
                    item {
                        Text(
                            text = "Son Egzersizler",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(uiState.recentSessions) { session ->
                        ExerciseHistoryItem(session = session)
                    }
                    
                    if (uiState.recentSessions.isNotEmpty()) {
                        item {
                            TextButton(
                                onClick = { /* TODO: Navigate to full history */ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tüm Geçmişi Görüntüle")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsOverview(
    totalExercises: Int,
    totalTime: Long,
    totalPoints: Int,
    currentStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Genel İstatistikler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatisticItem(
                    icon = Icons.Default.FitnessCenter,
                    label = "Toplam Egzersiz",
                    value = totalExercises.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatisticItem(
                    icon = Icons.Default.AccessTime,
                    label = "Toplam Süre",
                    value = "${totalTime / 60} saat",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatisticItem(
                    icon = Icons.Default.Star,
                    label = "Toplam Puan",
                    value = totalPoints.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                StatisticItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Günlük Seri",
                    value = "$currentStreak gün",
                    color = Color(0xFFFF6B35)
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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
fun WeeklyProgressCard(
    weeklyData: List<Pair<String, Int>>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Haftalık İlerleme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple bar chart representation
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weeklyData) { (day, count) ->
                    WeeklyProgressBar(
                        day = day,
                        exerciseCount = count,
                        maxCount = weeklyData.maxOfOrNull { it.second } ?: 1
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressBar(
    day: String,
    exerciseCount: Int,
    maxCount: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(40.dp)
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {}
            }
            
            // Progress bar
            if (exerciseCount > 0) {
                val progress = (exerciseCount.toFloat() / maxCount).coerceIn(0.1f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(progress)
                        .padding(vertical = 2.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {}
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = exerciseCount.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = day,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ExerciseCategoryChart(
    categoryData: List<Pair<String, Int>>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Egzersiz Kategorileri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (categoryData.isNotEmpty()) {
                categoryData.forEach { (category, count) ->
                    CategoryProgressItem(
                        category = category,
                        count = count,
                        total = categoryData.sumOf { it.second }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz egzersiz verisi bulunmuyor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryProgressItem(
    category: String,
    count: Int,
    total: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$count egzersiz",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = if (total > 0) count.toFloat() / total else 0f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ExerciseHistoryItem(
    session: ExerciseSession
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (session.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Egzersiz #${session.id.take(8)}", // Placeholder - in real app would show exercise name
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = android.text.format.DateFormat.getDateTimeInstance().format(session.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                if (session.completed) {
                    Text(
                        text = "Süre: ${session.duration / 60} dk | Puan: ${session.pointsEarned}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (session.completed) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "+${session.pointsEarned}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "puan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}