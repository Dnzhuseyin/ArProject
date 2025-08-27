package com.example.my_app.data.database

import androidx.room.*
import com.example.my_app.data.model.ExerciseSession
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExerciseSessionDao {
    
    @Query("SELECT * FROM exercise_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getUserSessions(userId: String): Flow<List<ExerciseSession>>
    
    @Query("SELECT * FROM exercise_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): ExerciseSession?
    
    @Query("SELECT * FROM exercise_sessions WHERE userId = :userId AND exerciseId = :exerciseId ORDER BY startTime DESC")
    fun getSessionsForExercise(userId: String, exerciseId: String): Flow<List<ExerciseSession>>
    
    @Query("SELECT * FROM exercise_sessions WHERE userId = :userId AND completed = 1 ORDER BY startTime DESC")
    fun getCompletedSessions(userId: String): Flow<List<ExerciseSession>>
    
    @Query("SELECT COUNT(*) FROM exercise_sessions WHERE userId = :userId AND completed = 1")
    suspend fun getCompletedSessionsCount(userId: String): Int
    
    @Query("SELECT SUM(duration) FROM exercise_sessions WHERE userId = :userId AND completed = 1")
    suspend fun getTotalExerciseTime(userId: String): Long?
    
    @Query("SELECT SUM(pointsEarned) FROM exercise_sessions WHERE userId = :userId AND completed = 1")
    suspend fun getTotalPointsEarned(userId: String): Int?
    
    @Query("SELECT * FROM exercise_sessions WHERE userId = :userId AND date(startTime/1000, 'unixepoch') = date('now') AND completed = 1")
    suspend fun getTodayCompletedSessions(userId: String): List<ExerciseSession>
    
    @Query("SELECT * FROM exercise_sessions WHERE userId = :userId AND startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    fun getSessionsInDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<ExerciseSession>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ExerciseSession)
    
    @Update
    suspend fun updateSession(session: ExerciseSession)
    
    @Delete
    suspend fun deleteSession(session: ExerciseSession)
    
    @Query("DELETE FROM exercise_sessions WHERE userId = :userId")
    suspend fun clearUserSessions(userId: String)
    
    @Query("DELETE FROM exercise_sessions")
    suspend fun clearAllSessions()
}