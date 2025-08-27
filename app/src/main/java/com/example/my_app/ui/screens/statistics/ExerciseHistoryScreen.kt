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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.my_app.data.model.ExerciseSession
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseHistoryScreen(
    navController: NavController,
    viewModel: ExerciseHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Egzersiz Geçmişi") },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.refreshHistory() }
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
                        HistoryStats(
                            totalSessions = uiState.sessions.size,
                            completedSessions = uiState.sessions.count { it.completed },
                            totalTime = uiState.sessions.filter { it.completed }.sumOf { it.duration },
                            totalPoints = uiState.sessions.filter { it.completed }.sumOf { it.pointsEarned }
                        )
                    }
                    
                    item {
                        FilterChips(
                            selectedFilter = uiState.selectedFilter,
                            onFilterSelected = viewModel::selectFilter
                        )
                    }
                    \n                    if (uiState.filteredSessions.isEmpty()) {\n                        item {\n                            Box(\n                                modifier = Modifier\n                                    .fillMaxWidth()\n                                    .height(200.dp),\n                                contentAlignment = Alignment.Center\n                            ) {\n                                Column(\n                                    horizontalAlignment = Alignment.CenterHorizontally\n                                ) {\n                                    Icon(\n                                        imageVector = Icons.Default.HistoryEdu,\n                                        contentDescription = null,\n                                        modifier = Modifier.size(64.dp),\n                                        tint = MaterialTheme.colorScheme.outline\n                                    )\n                                    Spacer(modifier = Modifier.height(8.dp))\n                                    Text(\n                                        text = \"Henüz egzersiz geçmişi bulunmuyor\",\n                                        style = MaterialTheme.typography.bodyLarge\n                                    )\n                                }\n                            }\n                        }\n                    } else {\n                        items(uiState.filteredSessions) { session ->\n                            ExerciseHistoryDetailCard(session = session)\n                        }\n                    }\n                }\n            }\n        }\n    }\n}\n\n@Composable\nfun HistoryStats(\n    totalSessions: Int,\n    completedSessions: Int,\n    totalTime: Long,\n    totalPoints: Int\n) {\n    Card(\n        modifier = Modifier.fillMaxWidth(),\n        colors = CardDefaults.cardColors(\n            containerColor = MaterialTheme.colorScheme.primaryContainer\n        )\n    ) {\n        Column(\n            modifier = Modifier.padding(20.dp)\n        ) {\n            Text(\n                text = \"Geçmiş Özeti\",\n                style = MaterialTheme.typography.titleLarge,\n                fontWeight = FontWeight.Bold,\n                color = MaterialTheme.colorScheme.onPrimaryContainer\n            )\n            \n            Spacer(modifier = Modifier.height(16.dp))\n            \n            Row(\n                modifier = Modifier.fillMaxWidth(),\n                horizontalArrangement = Arrangement.SpaceAround\n            ) {\n                HistoryStatItem(\n                    icon = Icons.Default.PlayCircleOutline,\n                    label = \"Toplam Seans\",\n                    value = totalSessions.toString()\n                )\n                \n                HistoryStatItem(\n                    icon = Icons.Default.CheckCircle,\n                    label = \"Tamamlanan\",\n                    value = completedSessions.toString()\n                )\n            }\n            \n            Spacer(modifier = Modifier.height(16.dp))\n            \n            Row(\n                modifier = Modifier.fillMaxWidth(),\n                horizontalArrangement = Arrangement.SpaceAround\n            ) {\n                HistoryStatItem(\n                    icon = Icons.Default.AccessTime,\n                    label = \"Toplam Süre\",\n                    value = \"${totalTime / 60} dk\"\n                )\n                \n                HistoryStatItem(\n                    icon = Icons.Default.Star,\n                    label = \"Toplam Puan\",\n                    value = totalPoints.toString()\n                )\n            }\n        }\n    }\n}\n\n@Composable\nfun HistoryStatItem(\n    icon: androidx.compose.ui.graphics.vector.ImageVector,\n    label: String,\n    value: String\n) {\n    Column(\n        horizontalAlignment = Alignment.CenterHorizontally\n    ) {\n        Icon(\n            imageVector = icon,\n            contentDescription = label,\n            tint = MaterialTheme.colorScheme.primary,\n            modifier = Modifier.size(28.dp)\n        )\n        Spacer(modifier = Modifier.height(4.dp))\n        Text(\n            text = value,\n            style = MaterialTheme.typography.titleMedium,\n            fontWeight = FontWeight.Bold,\n            color = MaterialTheme.colorScheme.onPrimaryContainer\n        )\n        Text(\n            text = label,\n            style = MaterialTheme.typography.bodySmall,\n            color = MaterialTheme.colorScheme.onPrimaryContainer\n        )\n    }\n}\n\n@Composable\nfun FilterChips(\n    selectedFilter: String,\n    onFilterSelected: (String) -> Unit\n) {\n    val filters = listOf(\n        \"Tümü\",\n        \"Tamamlanan\",\n        \"Devam Eden\",\n        \"Bu Hafta\",\n        \"Bu Ay\"\n    )\n    \n    LazyRow(\n        horizontalArrangement = Arrangement.spacedBy(8.dp)\n    ) {\n        items(filters) { filter ->\n            FilterChip(\n                onClick = { onFilterSelected(filter) },\n                label = { Text(filter) },\n                selected = selectedFilter == filter\n            )\n        }\n    }\n}\n\n@Composable\nfun ExerciseHistoryDetailCard(\n    session: ExerciseSession\n) {\n    Card(\n        modifier = Modifier.fillMaxWidth()\n    ) {\n        Column(\n            modifier = Modifier.padding(16.dp)\n        ) {\n            // Header with date and status\n            Row(\n                modifier = Modifier.fillMaxWidth(),\n                horizontalArrangement = Arrangement.SpaceBetween,\n                verticalAlignment = Alignment.Top\n            ) {\n                Column {\n                    Text(\n                        text = \"Egzersiz Seansı\",\n                        style = MaterialTheme.typography.titleMedium,\n                        fontWeight = FontWeight.Bold\n                    )\n                    \n                    Text(\n                        text = SimpleDateFormat(\"dd MMM yyyy, HH:mm\", Locale.getDefault())\n                            .format(session.startTime),\n                        style = MaterialTheme.typography.bodyMedium,\n                        color = MaterialTheme.colorScheme.outline\n                    )\n                }\n                \n                // Status badge\n                AssistChip(\n                    onClick = { },\n                    label = {\n                        Text(\n                            text = if (session.completed) \"Tamamlandı\" else \"Devam Ediyor\",\n                            style = MaterialTheme.typography.bodySmall\n                        )\n                    },\n                    leadingIcon = {\n                        Icon(\n                            imageVector = if (session.completed) \n                                Icons.Default.CheckCircle \n                            else \n                                Icons.Default.Schedule,\n                            contentDescription = null,\n                            modifier = Modifier.size(16.dp)\n                        )\n                    }\n                )\n            }\n            \n            if (session.completed) {\n                Spacer(modifier = Modifier.height(12.dp))\n                \n                // Session details\n                Row(\n                    modifier = Modifier.fillMaxWidth(),\n                    horizontalArrangement = Arrangement.SpaceBetween\n                ) {\n                    SessionDetailItem(\n                        icon = Icons.Default.AccessTime,\n                        label = \"Süre\",\n                        value = \"${session.duration / 60} dk\"\n                    )\n                    \n                    SessionDetailItem(\n                        icon = Icons.Default.FitnessCenter,\n                        label = \"Setler\",\n                        value = \"${session.completedSets}\"\n                    )\n                    \n                    SessionDetailItem(\n                        icon = Icons.Default.Star,\n                        label = \"Puan\",\n                        value = \"+${session.pointsEarned}\"\n                    )\n                    \n                    if (session.accuracy > 0) {\n                        SessionDetailItem(\n                            icon = Icons.Default.Accuracy,\n                            label = \"Doğruluk\",\n                            value = \"${(session.accuracy * 100).toInt()}%\"\n                        )\n                    }\n                }\n                \n                // Progress indicators\n                if (session.notes.isNotEmpty()) {\n                    Spacer(modifier = Modifier.height(12.dp))\n                    \n                    Text(\n                        text = \"Notlar: ${session.notes}\",\n                        style = MaterialTheme.typography.bodySmall,\n                        color = MaterialTheme.colorScheme.outline\n                    )\n                }\n                \n                // Pain level and mood indicators\n                if (session.painLevel > 0 || session.mood.isNotEmpty()) {\n                    Spacer(modifier = Modifier.height(8.dp))\n                    \n                    Row {\n                        if (session.painLevel > 0) {\n                            AssistChip(\n                                onClick = { },\n                                label = { Text(\"Ağrı: ${session.painLevel}/10\") }\n                            )\n                            Spacer(modifier = Modifier.width(8.dp))\n                        }\n                        \n                        if (session.mood.isNotEmpty()) {\n                            AssistChip(\n                                onClick = { },\n                                label = { Text(\"Ruh hali: ${session.mood}\") }\n                            )\n                        }\n                    }\n                }\n            }\n        }\n    }\n}\n\n@Composable\nfun SessionDetailItem(\n    icon: androidx.compose.ui.graphics.vector.ImageVector,\n    label: String,\n    value: String\n) {\n    Column(\n        horizontalAlignment = Alignment.CenterHorizontally\n    ) {\n        Icon(\n            imageVector = icon,\n            contentDescription = label,\n            tint = MaterialTheme.colorScheme.primary,\n            modifier = Modifier.size(20.dp)\n        )\n        Spacer(modifier = Modifier.height(2.dp))\n        Text(\n            text = value,\n            style = MaterialTheme.typography.bodyMedium,\n            fontWeight = FontWeight.Bold\n        )\n        Text(\n            text = label,\n            style = MaterialTheme.typography.bodySmall,\n            color = MaterialTheme.colorScheme.outline\n        )\n    }\n}