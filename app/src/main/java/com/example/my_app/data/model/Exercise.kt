package com.example.my_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val duration: Int = 0, // in seconds
    val difficulty: ExerciseDifficulty = ExerciseDifficulty.BEGINNER,
    val category: ExerciseCategory = ExerciseCategory.GENERAL,
    val targetMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val repetitions: Int = 0,
    val sets: Int = 0,
    val restTime: Int = 0, // seconds between sets
    val calories: Int = 0,
    val points: Int = 10,
    val requiresSensors: Boolean = false,
    val sensorTypes: List<String> = emptyList(), // accelerometer, gyroscope, etc.
    val isAssigned: Boolean = false,
    val assignedBy: String = "", // doctor/therapist ID
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ExerciseDifficulty {
    BEGINNER, INTERMEDIATE, ADVANCED
}

enum class ExerciseCategory {
    GENERAL, SHOULDER, KNEE, BACK, NECK, ANKLE, WRIST, HIP
}