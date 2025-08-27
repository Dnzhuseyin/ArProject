package com.example.my_app.data.repository

import com.example.my_app.data.database.MessageDao
import com.example.my_app.data.model.Message
import com.example.my_app.data.model.MessageType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val firestore: FirebaseFirestore
) {
    
    suspend fun sendMessage(
        senderId: String,
        receiverId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        attachmentUrl: String = ""
    ): Result<Message> {
        return try {
            val conversationId = generateConversationId(senderId, receiverId)
            val message = Message(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                messageType = messageType,
                attachmentUrl = attachmentUrl,
                timestamp = Date(),
                conversationId = conversationId
            )
            
            // Save to local database
            messageDao.insertMessage(message)
            
            // Save to Firestore
            firestore.collection("messages")
                .document(message.id)
                .set(message)
                .await()
            
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesForConversation(conversationId)
    }
    
    fun getUserConversations(userId: String): Flow<List<Message>> {
        return messageDao.getUserMessages(userId)
    }
    
    fun getUnreadMessagesCount(userId: String): Flow<Int> {
        return messageDao.getUnreadMessagesCount(userId)
    }
    
    suspend fun markMessagesAsRead(conversationId: String, userId: String): Result<Unit> {
        return try {
            messageDao.markMessagesAsRead(conversationId, userId)
            
            // Update in Firestore
            val batch = firestore.batch()
            val snapshot = firestore.collection("messages")
                .whereEqualTo("conversationId", conversationId)
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            snapshot.documents.forEach { document ->
                batch.update(document.reference, "isRead", true)
            }
            
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncMessages(userId: String): Result<Unit> {
        return try {
            // Fetch messages from Firestore
            val snapshot = firestore.collection("messages")
                .whereIn("senderId", listOf(userId))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
            
            val messages = snapshot.toObjects(Message::class.java)
            
            // Save to local database
            messages.forEach { message ->
                messageDao.insertMessage(message)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateConversationId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }
    
    suspend fun getOrCreatePhysiotherapistConversation(userId: String): String {
        // In a real app, you'd fetch the assigned physiotherapist
        // For now, using a placeholder physiotherapist ID
        val physiotherapistId = "physiotherapist_${userId.take(8)}"
        return generateConversationId(userId, physiotherapistId)
    }
    
    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            // Delete from local database
            val message = messageDao.getMessagesForConversation("").first { it.id == messageId }
            messageDao.deleteMessage(message)
            
            // Delete from Firestore
            firestore.collection("messages")
                .document(messageId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}