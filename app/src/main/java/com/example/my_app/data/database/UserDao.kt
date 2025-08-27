package com.example.my_app.data.database

import androidx.room.*
import com.example.my_app.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<User?>
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("UPDATE users SET totalPoints = :points WHERE id = :userId")
    suspend fun updateUserPoints(userId: String, points: Int)
    
    @Query("UPDATE users SET completedExercises = :count WHERE id = :userId")
    suspend fun updateCompletedExercises(userId: String, count: Int)
    
    @Query("UPDATE users SET totalExerciseTime = :time WHERE id = :userId")
    suspend fun updateTotalExerciseTime(userId: String, time: Long)
    
    @Query("UPDATE users SET streakDays = :streak WHERE id = :userId")
    suspend fun updateStreak(userId: String, streak: Int)
    
    @Query("SELECT * FROM users ORDER BY totalPoints DESC LIMIT :limit")
    fun getLeaderboard(limit: Int = 50): Flow<List<User>>
    
    @Query("DELETE FROM users")
    suspend fun clearUsers()
}