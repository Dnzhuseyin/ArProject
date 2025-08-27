package com.example.my_app.ui.screens.communication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    navController: NavController,
    userId: String
) {
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOn by remember { mutableStateOf(true) }
    var callDuration by remember { mutableStateOf("00:00") }
    
    // Simulate call timer (placeholder)
    LaunchedEffect(Unit) {
        // This would be replaced with actual video call logic
        var seconds = 0
        while (true) {
            kotlinx.coroutines.delay(1000)
            seconds++
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            callDuration = String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top bar with call info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dr. Ahmet Yılmaz",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fizyoterapist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = callDuration,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Video placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp, bottom = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isVideoOn) {
                // Main video area (placeholder)
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoCall,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Video Arama Başlatılıyor...",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Gerçek video arama özelliği gelecek güncellemelerde eklenecektir.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 8.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }
                
                // Small self video placeholder (top right corner)
                Card(
                    modifier = Modifier
                        .size(120.dp, 160.dp)
                        .offset(x = (-16).dp, y = 16.dp)
                        .align(Alignment.TopEnd),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Gray.copy(alpha = 0.8f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Siz",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }
                }
            } else {
                // Video off state
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideocamOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Video Kapalı",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom control panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute button
                FloatingActionButton(
                    onClick = { isMuted = !isMuted },
                    containerColor = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isMuted) "Sesi Aç" else "Sesi Kapat",
                        tint = if (isMuted) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
                
                // End call button
                FloatingActionButton(
                    onClick = { navController.popBackStack() },
                    containerColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Aramayı Sonlandır",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Video toggle button
                FloatingActionButton(
                    onClick = { isVideoOn = !isVideoOn },
                    containerColor = if (!isVideoOn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = if (isVideoOn) "Videoyu Kapat" else "Videoyu Aç",
                        tint = if (!isVideoOn) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}