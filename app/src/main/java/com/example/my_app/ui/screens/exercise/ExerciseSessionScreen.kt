package com.example.my_app.ui.screens.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSessionScreen(
    navController: NavController,
    exerciseId: String,
    viewModel: ExerciseSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(exerciseId) {
        viewModel.startExerciseSession(exerciseId)
    }
    
    // Handle session completion
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            delay(2000) // Show completion for 2 seconds
            navController.popBackStack()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text(uiState.exercise?.title ?: "Egzersiz Seansı") },
            navigationIcon = {
                IconButton(
                    onClick = { 
                        viewModel.pauseSession()
                        navController.popBackStack() 
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Çıkış"
                    )
                }
            },
            actions = {
                if (uiState.isPaused) {
                    TextButton(
                        onClick = { viewModel.resumeSession() }
                    ) {
                        Text("Devam Et")
                    }
                } else {
                    TextButton(
                        onClick = { viewModel.pauseSession() }
                    ) {
                        Text("Duraklat")
                    }
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
            
            uiState.isCompleted -> {
                ExerciseCompletionScreen(
                    session = uiState,
                    onContinue = { navController.popBackStack() }
                )
            }
            
            uiState.exercise != null -> {
                ExerciseSessionContent(
                    uiState = uiState,
                    onStartSet = viewModel::startSet,
                    onCompleteSet = viewModel::completeSet,
                    onCompleteExercise = viewModel::completeSession,
                    onUpdateReps = viewModel::updateCurrentReps
                )
            }
        }
    }
}

@Composable
fun ExerciseSessionContent(
    uiState: ExerciseSessionUiState,
    onStartSet: () -> Unit,
    onCompleteSet: () -> Unit,
    onCompleteExercise: () -> Unit,
    onUpdateReps: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer/Progress Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set ${uiState.currentSet}/${uiState.exercise?.sets ?: 0}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress ring
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = if (uiState.exercise?.sets != null && uiState.exercise.sets > 0) {
                            uiState.currentSet.toFloat() / uiState.exercise.sets
                        } else 0f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.timerText,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isResting) "Dinlenme" else "Egzersiz",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Set progress
                LinearProgressIndicator(
                    progress = if (uiState.exercise?.sets != null && uiState.exercise.sets > 0) {
                        uiState.currentSet.toFloat() / uiState.exercise.sets
                    } else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Reps Counter
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tekrar Sayısı",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { 
                            if (uiState.currentReps > 0) {
                                onUpdateReps(uiState.currentReps - 1)
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Azalt",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Text(
                        text = "${uiState.currentReps}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 80.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = { onUpdateReps(uiState.currentReps + 1) },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Artır",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Text(
                    text = "Hedef: ${uiState.exercise?.repetitions ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sensor Data (if available)
        if (uiState.sensorData.isNotEmpty()) {
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
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sensör Verileri",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SensorDataItem(
                            label = "Hareket",
                            value = "${uiState.movementCount}",
                            icon = Icons.Default.DirectionsRun
                        )
                        
                        SensorDataItem(
                            label = "Doğruluk",
                            value = "${(uiState.accuracy * 100).toInt()}%",
                            icon = Icons.Default.CheckCircle
                        )
                        
                        SensorDataItem(
                            label = "Kalori",
                            value = "${uiState.caloriesBurned}",
                            icon = Icons.Default.LocalFireDepartment
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.currentSet < (uiState.exercise?.sets ?: 0)) {
                OutlinedButton(
                    onClick = onCompleteSet,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isResting
                ) {
                    Text("Set Tamamla")
                }
                
                Button(
                    onClick = onStartSet,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isResting || uiState.currentSet == 0
                ) {
                    Text(if (uiState.currentSet == 0) "Başla" else "Devam Et")
                }
            } else {
                Button(
                    onClick = onCompleteExercise,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Egzersizi Tamamla")
                }
            }
        }
    }
}

@Composable
fun SensorDataItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ExerciseCompletionScreen(
    session: ExerciseSessionUiState,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Tebrikler!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Egzersizi başarıyla tamamladınız",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stats
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Seansınız",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        label = "Süre",
                        value = session.timerText
                    )
                    
                    StatItem(
                        label = "Setler",
                        value = "${session.currentSet}"
                    )
                    
                    StatItem(
                        label = "Puan",
                        value = "+${session.pointsEarned}"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ana Sayfaya Dön")
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}