package com.example.my_app.data.repository

import com.example.my_app.data.database.UserDao
import com.example.my_app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    fun getCurrentUser(): Flow<User?> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(null)
        return userDao.getUserById(currentUserId)
    }
    
    suspend fun getUserById(userId: String): User? {
        return try {
            // Try local database first
            val localUser = userDao.getUserById(userId)
            
            // If not found locally, fetch from Firestore
            if (localUser == null) {
                val document = firestore.collection("users").document(userId).get().await()
                val user = document.toObject(User::class.java)
                user?.let { userDao.insertUser(it) }
                user
            } else {
                null // Will return from Flow
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun createUser(user: User): Result<User> {
        return try {
            // Save to Firestore
            firestore.collection("users").document(user.id).set(user).await()
            
            // Save to local database
            userDao.insertUser(user)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(user: User): Result<User> {
        return try {
            // Update in Firestore
            firestore.collection("users").document(user.id).set(user).await()
            
            // Update in local database
            userDao.updateUser(user)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserPoints(userId: String, points: Int): Result<Unit> {
        return try {
            // Update in Firestore
            firestore.collection("users").document(userId).update("totalPoints", points).await()
            
            // Update in local database
            userDao.updateUserPoints(userId, points)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserStats(userId: String, completedExercises: Int, totalTime: Long): Result<Unit> {
        return try {
            val updates = mapOf(
                "completedExercises" to completedExercises,
                "totalExerciseTime" to totalTime
            )
            
            firestore.collection("users").document(userId).update(updates).await()
            userDao.updateCompletedExercises(userId, completedExercises)
            userDao.updateTotalExerciseTime(userId, totalTime)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getLeaderboard(limit: Int = 50): Flow<List<User>> {
        return userDao.getLeaderboard(limit)
    }
    
    suspend fun syncUserData() {
        try {
            // Fetch all users from Firestore and update local database
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.toObjects(User::class.java)
            
            userDao.clearUsers()
            users.forEach { userDao.insertUser(it) }
        } catch (e: Exception) {
            // Handle sync error
        }
    }
    
    suspend fun signOut() {
        firebaseAuth.signOut()
        userDao.clearUsers()
    }
}