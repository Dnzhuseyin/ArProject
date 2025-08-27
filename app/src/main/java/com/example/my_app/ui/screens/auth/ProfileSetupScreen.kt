package com.example.my_app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.my_app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.profileSetupCompleted) {
        if (uiState.profileSetupCompleted) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.ProfileSetup.route) { inclusive = true }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = 0.75f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Header
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Profil Kurulumu",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Size daha iyi hizmet verebilmemiz için birkaç soru cevaplayın",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Questions Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Question 1: Are you an athlete?
                QuestionCard(
                    question = "Sporcu musunuz?",
                    subtitle = "Bu bilgi egzersiz planınızı kişiselleştirmemize yardımcı olur",
                    selectedValue = uiState.isAthlete,
                    onValueChange = viewModel::updateIsAthlete,
                    options = listOf(
                        true to "Evet, aktif olarak spor yapıyorum",
                        false to "Hayır, sporcu değilim"
                    )
                )
                
                Divider()
                
                // Question 2: Have you had surgery?
                QuestionCard(
                    question = "Daha önce ameliyat geçirdiniz mi?",
                    subtitle = "Bu bilgi güvenli egzersiz programı hazırlamamıza yardımcı olur",
                    selectedValue = uiState.hasSurgery,
                    onValueChange = viewModel::updateHasSurgery,
                    options = listOf(
                        true to "Evet, ameliyat geçirdim",
                        false to "Hayır, ameliyat geçirmedim"
                    )
                )
                
                // Surgery details (conditional)
                if (uiState.hasSurgery == true) {
                    OutlinedTextField(
                        value = uiState.surgeryDetails,
                        onValueChange = viewModel::updateSurgeryDetails,
                        label = { Text("Ameliyat Detayları") },
                        placeholder = { Text("Hangi ameliyatı geçirdiniz? (Opsiyonel)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
                
                Divider()
                
                // Question 3: Doctor information
                Column {
                    Text(
                        text = "Hangi doktordan fizik tedavi alıyorsunuz?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Doktorunuzla iletişime geçebilmemiz için gereklidir",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = uiState.doctorName,
                        onValueChange = viewModel::updateDoctorName,
                        label = { Text("Doktor Adı") },
                        placeholder = { Text("Dr. Ad Soyad") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    // Skip for now
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Şimdilik Atla")
            }
            
            Button(
                onClick = viewModel::completeProfileSetup,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading && 
                         uiState.isAthlete != null && 
                         uiState.hasSurgery != null &&
                         uiState.doctorName.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Tamamla")
                }
            }
        }
        
        // Error Message
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: String,
    subtitle: String,
    selectedValue: Boolean?,
    onValueChange: (Boolean) -> Unit,
    options: List<Pair<Boolean, String>>
) {
    Column {
        Text(
            text = question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        options.forEach { (value, text) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedValue == value,
                    onClick = { onValueChange(value) }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}