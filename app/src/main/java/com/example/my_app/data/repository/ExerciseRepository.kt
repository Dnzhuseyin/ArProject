package com.example.my_app.data.repository

import com.example.my_app.data.database.ExerciseDao
import com.example.my_app.data.database.ExerciseSessionDao
import com.example.my_app.data.model.Exercise
import com.example.my_app.data.model.ExerciseCategory
import com.example.my_app.data.model.ExerciseDifficulty
import com.example.my_app.data.model.ExerciseSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val exerciseSessionDao: ExerciseSessionDao,
    private val firestore: FirebaseFirestore
) {
    
    fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises()
    }
    
    fun getAssignedExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAssignedExercises()
    }
    
    fun getExercisesByCategory(category: ExerciseCategory): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByCategory(category)
    }
    
    fun getExercisesByDifficulty(difficulty: ExerciseDifficulty): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByDifficulty(difficulty)
    }
    
    suspend fun getExerciseById(exerciseId: String): Exercise? {
        return exerciseDao.getExerciseById(exerciseId)
    }
    
    fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.searchExercises(query)
    }
    
    suspend fun syncExercises(): Result<Unit> {
        return try {
            val snapshot = firestore.collection("exercises").get().await()
            val exercises = snapshot.toObjects(Exercise::class.java)
            
            exerciseDao.clearExercises()
            exerciseDao.insertExercises(exercises)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun assignExercise(exerciseId: String, userId: String): Result<Unit> {
        return try {
            exerciseDao.updateAssignmentStatus(exerciseId, true)
            
            // Update in Firestore as well
            firestore.collection("exercises").document(exerciseId)
                .update("isAssigned", true, "assignedTo", userId).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Exercise Session methods
    fun getUserSessions(userId: String): Flow<List<ExerciseSession>> {
        return exerciseSessionDao.getUserSessions(userId)
    }
    
    fun getSessionsForExercise(userId: String, exerciseId: String): Flow<List<ExerciseSession>> {
        return exerciseSessionDao.getSessionsForExercise(userId, exerciseId)
    }
    
    fun getCompletedSessions(userId: String): Flow<List<ExerciseSession>> {
        return exerciseSessionDao.getCompletedSessions(userId)
    }
    
    suspend fun startExerciseSession(session: ExerciseSession): Result<ExerciseSession> {
        return try {
            exerciseSessionDao.insertSession(session)
            
            // Save to Firestore
            firestore.collection("exercise_sessions").document(session.id).set(session).await()
            
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun completeExerciseSession(session: ExerciseSession): Result<ExerciseSession> {
        return try {
            val completedSession = session.copy(
                completed = true,
                endTime = java.util.Date(),
                duration = if (session.endTime != null) {
                    (session.endTime!!.time - session.startTime.time) / 1000
                } else 0
            )
            
            exerciseSessionDao.updateSession(completedSession)
            
            // Update in Firestore
            firestore.collection("exercise_sessions").document(session.id).set(completedSession).await()
            
            Result.success(completedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserStats(userId: String): Triple<Int, Long, Int> {
        val completedCount = exerciseSessionDao.getCompletedSessionsCount(userId)
        val totalTime = exerciseSessionDao.getTotalExerciseTime(userId) ?: 0L
        val totalPoints = exerciseSessionDao.getTotalPointsEarned(userId) ?: 0
        
        return Triple(completedCount, totalTime, totalPoints)
    }
    
    suspend fun getTodayCompletedSessions(userId: String): List<ExerciseSession> {
        return exerciseSessionDao.getTodayCompletedSessions(userId)
    }
}