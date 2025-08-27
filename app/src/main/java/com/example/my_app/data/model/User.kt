package com.example.my_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val fullName: String = "",
    val profileImageUrl: String = "",
    val isAthlete: Boolean = false,
    val hasSurgery: Boolean = false,
    val surgeryDetails: String = "",
    val doctorName: String = "",
    val totalPoints: Int = 0,
    val level: Int = 1,
    val streakDays: Int = 0,
    val joinDate: Date = Date(),
    val lastActive: Date = Date(),
    val completedExercises: Int = 0,
    val totalExerciseTime: Long = 0L, // in minutes
    val isOnline: Boolean = false,
    val fcmToken: String = ""
)