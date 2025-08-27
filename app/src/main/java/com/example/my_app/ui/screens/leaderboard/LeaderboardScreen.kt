package com.example.my_app.ui.screens.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class LeaderboardUser(
    val id: String,
    val name: String,
    val points: Int,
    val level: Int,
    val isCurrentUser: Boolean = false
)

@Composable
fun LeaderboardScreen(navController: NavController) {
    // Mock data for leaderboard
    val leaderboardUsers = listOf(
        LeaderboardUser("1", "Ahmet Yıldız", 2150, 8),
        LeaderboardUser("2", "Ayşe Kaya", 1980, 7),
        LeaderboardUser("3", "Mehmet Demir", 1850, 7),
        LeaderboardUser("4", "Fatma Şahin", 1720, 6),
        LeaderboardUser("5", "Kullanıcı Adı", 1240, 5, true), // Current user
        LeaderboardUser("6", "Ali Öz", 1100, 5),
        LeaderboardUser("7", "Zeynep Kılıç", 980, 4),
        LeaderboardUser("8", "Can Arslan", 850, 4)
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri"
                )
            }
            
            Text(
                text = "Liderlik Tablosu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Current user rank card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Sıralamanız",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "5. sırada - 1,240 puan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "En İyi 10",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(leaderboardUsers) { index, user ->
                LeaderboardItem(
                    rank = index + 1,
                    user = user
                )
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    rank: Int,
    user: LeaderboardUser
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (user.isCurrentUser) {
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
            // Rank
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                when (rank) {
                    1 -> Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "1. sıra",
                        tint = androidx.compose.ui.graphics.Color(0xFFFFD700), // Gold
                        modifier = Modifier.size(32.dp)
                    )
                    2 -> Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "2. sıra",
                        tint = androidx.compose.ui.graphics.Color(0xFFC0C0C0), // Silver
                        modifier = Modifier.size(28.dp)
                    )
                    3 -> Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "3. sıra",
                        tint = androidx.compose.ui.graphics.Color(0xFFCD7F32), // Bronze
                        modifier = Modifier.size(24.dp)
                    )
                    else -> Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User avatar
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (user.isCurrentUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (user.isCurrentUser) "${user.name} (Sen)" else user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (user.isCurrentUser) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = "Seviye ${user.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Points
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${user.points}",
                    style = MaterialTheme.typography.titleMedium,
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