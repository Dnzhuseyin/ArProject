package com.example.my_app.data.database

import androidx.room.*
import com.example.my_app.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE (senderId = :userId OR receiverId = :userId) ORDER BY timestamp DESC")
    fun getUserMessages(userId: String): Flow<List<Message>>
    
    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadMessagesCount(userId: String): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND receiverId = :userId")
    suspend fun markMessagesAsRead(conversationId: String, userId: String)
    
    @Delete
    suspend fun deleteMessage(message: Message)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)
    
    @Query("DELETE FROM messages")
    suspend fun clearMessages()
}