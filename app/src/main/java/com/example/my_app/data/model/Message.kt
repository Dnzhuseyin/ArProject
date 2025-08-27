package com.example.my_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val messageType: MessageType = MessageType.TEXT,
    val attachmentUrl: String = "",
    val timestamp: Date = Date(),
    val isRead: Boolean = false,
    val conversationId: String = ""
)

enum class MessageType {
    TEXT, IMAGE, VIDEO, EXERCISE_REPORT, AUDIO
}