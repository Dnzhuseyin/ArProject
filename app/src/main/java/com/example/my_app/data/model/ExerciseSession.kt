package com.example.my_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "exercise_sessions")
data class ExerciseSession(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val startTime: Date = Date(),
    val endTime: Date? = null,
    val duration: Long = 0L, // in seconds
    val completed: Boolean = false,
    val completedSets: Int = 0,
    val completedReps: Int = 0,
    val caloriesBurned: Int = 0,
    val pointsEarned: Int = 0,
    val accuracy: Float = 0f, // for sensor-tracked exercises (0-100%)
    val notes: String = "",
    val sensorData: String = "", // JSON string of sensor readings
    val painLevel: Int = 0, // 1-10 scale
    val difficultyFeedback: Int = 0, // 1-5 scale (too easy to too hard)
    val mood: String = "", // before/after exercise mood
    val createdAt: Long = System.currentTimeMillis()
)