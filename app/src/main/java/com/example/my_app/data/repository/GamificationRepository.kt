package com.example.my_app.data.repository

import com.example.my_app.data.database.AchievementDao
import com.example.my_app.data.database.ExerciseSessionDao
import com.example.my_app.data.database.UserDao
import com.example.my_app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationRepository @Inject constructor(
    private val userDao: UserDao,
    private val achievementDao: AchievementDao,
    private val exerciseSessionDao: ExerciseSessionDao
) {
    
    suspend fun awardPoints(userId: String, points: Int, reason: String): Result<Unit> {
        return try {
            val currentUser = userDao.getUserById(userId).first()
            if (currentUser != null) {
                val newTotalPoints = currentUser.totalPoints + points
                val newLevel = calculateLevel(newTotalPoints)
                
                val updatedUser = currentUser.copy(
                    totalPoints = newTotalPoints,
                    level = newLevel
                )
                
                userDao.updateUser(updatedUser)
                
                // Check for achievements
                checkAndUnlockAchievements(userId, updatedUser)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateStreakDays(userId: String): Result<Int> {
        return try {
            val todaySessions = exerciseSessionDao.getTodayCompletedSessions(userId)
            
            if (todaySessions.isNotEmpty()) {
                val currentUser = userDao.getUserById(userId).first()
                if (currentUser != null) {
                    val newStreak = currentUser.streakDays + 1
                    userDao.updateStreak(userId, newStreak)
                    
                    // Award streak bonus points
                    if (newStreak % 7 == 0) { // Weekly streak bonus
                        awardPoints(userId, 50, "Weekly streak bonus")
                    }
                    
                    Result.success(newStreak)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Result.success(0) // No exercises completed today
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getAllAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAllAchievements()
    }
    
    fun getUnlockedAchievements(): Flow<List<Achievement>> {
        return achievementDao.getUnlockedAchievements()
    }
    
    fun getDailyTasks(date: Date): Flow<List<DailyTask>> {
        return achievementDao.getDailyTasks(date)
    }
    
    suspend fun completeTask(taskId: String): Result<Unit> {
        return try {
            achievementDao.completeTask(taskId)
            
            // Get the completed task to award points
            val allTasks = achievementDao.getDailyTasks(Date()).first()
            val completedTask = allTasks.find { it.id == taskId && it.isCompleted }
            
            completedTask?.let { task ->
                // Award points for completing the task (you'd need user ID here)
                // awardPoints(userId, task.points, "Daily task completed: ${task.title}")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun generateDailyTasks(userId: String, date: Date): Result<List<DailyTask>> {
        return try {
            val existingTasks = achievementDao.getDailyTasks(date).first()
            
            if (existingTasks.isEmpty()) {
                val tasks = createDailyTasksForUser(userId, date)
                achievementDao.insertDailyTasks(tasks)
                Result.success(tasks)
            } else {
                Result.success(existingTasks)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun checkAndUnlockAchievements(userId: String, user: User) {
        val allAchievements = achievementDao.getAllAchievements().first()
        val unlockedAchievements = allAchievements.filter { !it.isUnlocked }
        
        unlockedAchievements.forEach { achievement ->
            when (achievement.category) {
                AchievementCategory.POINTS -> {
                    if (user.totalPoints >= achievement.requiredValue) {
                        unlockAchievement(achievement.id)
                    }
                }
                AchievementCategory.STREAK -> {
                    if (user.streakDays >= achievement.requiredValue) {
                        unlockAchievement(achievement.id)
                    }
                }
                AchievementCategory.EXERCISES -> {
                    if (user.completedExercises >= achievement.requiredValue) {
                        unlockAchievement(achievement.id)
                    }
                }
                else -> {
                    // Handle other achievement types
                }
            }
        }
    }
    
    private suspend fun unlockAchievement(achievementId: String) {
        achievementDao.unlockAchievement(achievementId, Date())
    }
    
    private fun calculateLevel(points: Int): Int {
        return when {
            points >= 10000 -> 10
            points >= 7500 -> 9
            points >= 5000 -> 8
            points >= 3500 -> 7
            points >= 2500 -> 6
            points >= 1500 -> 5
            points >= 1000 -> 4
            points >= 500 -> 3
            points >= 250 -> 2
            points >= 100 -> 1
            else -> 0
        }
    }
    
    private fun createDailyTasksForUser(userId: String, date: Date): List<DailyTask> {
        return listOf(
            DailyTask(
                id = UUID.randomUUID().toString(),
                title = "3 Egzersiz Tamamla",
                description = "Bugün en az 3 egzersiz tamamlayın",
                targetValue = 3,
                points = 50,
                taskType = TaskType.EXERCISE_COUNT,
                date = date,
                expiresAt = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.DAY_OF_MONTH, 1)
                }.time
            ),
            DailyTask(
                id = UUID.randomUUID().toString(),
                title = "30 Dakika Egzersiz",
                description = "Toplam 30 dakika egzersiz yapın",
                targetValue = 30,
                points = 30,
                taskType = TaskType.EXERCISE_TIME,
                date = date,
                expiresAt = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.DAY_OF_MONTH, 1)
                }.time
            ),
            DailyTask(
                id = UUID.randomUUID().toString(),
                title = "100 Puan Kazan",
                description = "Bugün 100 puan kazanın",
                targetValue = 100,
                points = 25,
                taskType = TaskType.POINTS_EARNED,
                date = date,
                expiresAt = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.DAY_OF_MONTH, 1)
                }.time
            )
        )
    }
    
    suspend fun initializeAchievements(): Result<Unit> {
        return try {
            val existingAchievements = achievementDao.getAllAchievements().first()
            
            if (existingAchievements.isEmpty()) {
                val defaultAchievements = createDefaultAchievements()
                achievementDao.insertAchievements(defaultAchievements)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createDefaultAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                id = "first_exercise",
                title = "İlk Adım",
                description = "İlk egzersiznizi tamamladınız!",
                category = AchievementCategory.EXERCISES,
                requiredValue = 1,
                points = 25
            ),
            Achievement(
                id = "exercise_master",
                title = "Egzersiz Ustası",
                description = "10 egzersiz tamamladınız",
                category = AchievementCategory.EXERCISES,
                requiredValue = 10,
                points = 100
            ),
            Achievement(
                id = "point_collector",
                title = "Puan Koleksiyoncusu",
                description = "1000 puan topladınız",
                category = AchievementCategory.POINTS,
                requiredValue = 1000,
                points = 200
            ),
            Achievement(
                id = "week_warrior",
                title = "Haftalık Savaşçı",
                description = "7 gün üst üste egzersiz yaptınız",
                category = AchievementCategory.STREAK,
                requiredValue = 7,
                points = 150
            ),
            Achievement(
                id = "consistency_king",
                title = "Tutarlılık Kralı",
                description = "30 gün üst üste egzersiz yaptınız",
                category = AchievementCategory.STREAK,
                requiredValue = 30,
                points = 500
            )
        )
    }
}