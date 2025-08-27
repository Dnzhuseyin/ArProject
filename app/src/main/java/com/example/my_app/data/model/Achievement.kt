package com.example.my_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val category: AchievementCategory = AchievementCategory.GENERAL,
    val requiredValue: Int = 0,
    val points: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Date? = null,
    val progress: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AchievementCategory {
    GENERAL, STREAK, EXERCISES, POINTS, CONSISTENCY, MILESTONES
}

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val targetValue: Int = 1,
    val currentProgress: Int = 0,
    val points: Int = 0,
    val taskType: TaskType = TaskType.EXERCISE_COUNT,
    val isCompleted: Boolean = false,
    val date: Date = Date(),
    val expiresAt: Date = Date()
)

enum class TaskType {
    EXERCISE_COUNT, EXERCISE_TIME, POINTS_EARNED, STREAK_MAINTAIN, SPECIFIC_EXERCISE
}