package com.example.my_app.ui.screens.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.model.Message
import com.example.my_app.data.model.MessageType
import com.example.my_app.data.repository.MessageRepository
import com.example.my_app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val currentUserId: String = "",
    val receiverName: String = "Fizyoterapist",
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var conversationId: String = ""
    
    fun loadMessages(conversationId: String) {
        this.conversationId = conversationId
        
        viewModelScope.launch {
            // Get current user
            userRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(currentUserId = user.id) }
                    
                    // Load messages for this conversation
                    messageRepository.getMessagesForConversation(conversationId).collect { messages ->
                        _uiState.update { 
                            it.copy(
                                messages = messages.sortedBy { message -> message.timestamp }
                            )
                        }
                        
                        // Mark messages as read
                        markMessagesAsRead()
                    }
                }
            }
        }
    }
    
    fun updateMessage(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
        
        // Simulate typing indicator (in real app, would send typing status to other user)
        if (message.isNotBlank()) {
            simulateTypingResponse()
        }
    }
    
    fun sendMessage() {
        val currentState = _uiState.value
        val messageContent = currentState.currentMessage.trim()
        
        if (messageContent.isBlank() || currentState.isSending) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            
            try {
                // Determine receiver ID (in real app, would be the actual physiotherapist ID)
                val receiverId = getPhysiotherapistId(currentState.currentUserId)
                
                val result = messageRepository.sendMessage(
                    senderId = currentState.currentUserId,
                    receiverId = receiverId,
                    content = messageContent,
                    messageType = MessageType.TEXT
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                currentMessage = "",
                                isSending = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isSending = false,
                                error = exception.message ?: "Mesaj gönderilemedi"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSending = false,
                        error = e.message ?: "Mesaj gönderilemedi"
                    )
                }
            }
        }
    }
    
    fun sendExerciseReport(exerciseSessionId: String, reportContent: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val receiverId = getPhysiotherapistId(currentState.currentUserId)
            
            messageRepository.sendMessage(
                senderId = currentState.currentUserId,
                receiverId = receiverId,
                content = reportContent,
                messageType = MessageType.EXERCISE_REPORT
            )
        }
    }
    
    private fun markMessagesAsRead() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (conversationId.isNotEmpty() && currentState.currentUserId.isNotEmpty()) {
                messageRepository.markMessagesAsRead(conversationId, currentState.currentUserId)
            }
        }
    }
    
    private fun simulateTypingResponse() {
        // Simulate physiotherapist typing (for demo purposes)
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(isTyping = true) }
            
            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(isTyping = false) }
            
            // Simulate automatic response
            if (Math.random() > 0.5) {
                sendAutomaticResponse()
            }
        }
    }
    
    private suspend fun sendAutomaticResponse() {
        val responses = listOf(
            "Teşekkür ederim. Egzersizlerinizi düzenli olarak yapmanız harika!",
            "İlerlemenizi takip ediyorum. Devam edin!",
            "Herhangi bir ağrı hissederseniz lütfen durdurun ve bana bildirin.",
            "Egzersizlerinizi tamamladığınız için tebrikler!",
            "Soru ve endişeleriniz için her zaman buradayım."
        )
        
        val currentState = _uiState.value
        val response = responses.random()
        val senderId = getPhysiotherapistId(currentState.currentUserId)
        
        messageRepository.sendMessage(
            senderId = senderId,
            receiverId = currentState.currentUserId,
            content = response,
            messageType = MessageType.TEXT
        )
    }
    
    private fun getPhysiotherapistId(userId: String): String {
        // In a real app, this would fetch the assigned physiotherapist
        return "physiotherapist_${userId.take(8)}"
    }
    
    fun refreshMessages() {
        if (conversationId.isNotEmpty()) {
            loadMessages(conversationId)
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}