package com.example.my_app.data.database

import androidx.room.*
import com.example.my_app.data.model.Achievement
import com.example.my_app.data.model.AchievementCategory
import com.example.my_app.data.model.DailyTask
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AchievementDao {
    
    // Achievements
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, points DESC")
    fun getAllAchievements(): Flow<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE category = :category")
    fun getAchievementsByCategory(category: AchievementCategory): Flow<List<Achievement>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)
    
    @Update
    suspend fun updateAchievement(achievement: Achievement)
    
    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: String, unlockedAt: Date)
    
    @Query("UPDATE achievements SET progress = :progress WHERE id = :achievementId")
    suspend fun updateAchievementProgress(achievementId: String, progress: Int)
    
    // Daily Tasks
    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY isCompleted ASC, points DESC")
    fun getDailyTasks(date: Date): Flow<List<DailyTask>>
    
    @Query("SELECT * FROM daily_tasks WHERE date = :date AND isCompleted = 0")
    fun getIncompleteDailyTasks(date: Date): Flow<List<DailyTask>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTask(task: DailyTask)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTasks(tasks: List<DailyTask>)
    
    @Update
    suspend fun updateDailyTask(task: DailyTask)
    
    @Query("UPDATE daily_tasks SET currentProgress = :progress WHERE id = :taskId")
    suspend fun updateTaskProgress(taskId: String, progress: Int)
    
    @Query("UPDATE daily_tasks SET isCompleted = 1 WHERE id = :taskId")
    suspend fun completeTask(taskId: String)
    
    @Query("DELETE FROM daily_tasks WHERE date < :date")
    suspend fun deleteExpiredTasks(date: Date)
    
    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()
    
    @Query("DELETE FROM daily_tasks")
    suspend fun clearDailyTasks()
}