package com.example.my_app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}

/**
 * Firebase collections and document references
 */
object FirebaseConstants {
    // Collections
    const val USERS_COLLECTION = "users"
    const val EXERCISES_COLLECTION = "exercises"
    const val SESSIONS_COLLECTION = "sessions"
    const val MESSAGES_COLLECTION = "messages"
    const val ACHIEVEMENTS_COLLECTION = "achievements"
    
    // Storage paths
    const val PROFILE_IMAGES_PATH = "profile_images"
    const val EXERCISE_VIDEOS_PATH = "exercise_videos"
    const val MESSAGE_ATTACHMENTS_PATH = "message_attachments"
}

/**
 * Firebase service for handling authentication
 */
class FirebaseAuthService(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid
    
    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null
    
    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password)
            val userId = result.result.user?.uid
            if (userId != null) {
                Result.success(userId)
            } else {
                Result.failure(Exception("Giriş başarısız"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun registerWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password)
            val userId = result.result.user?.uid
            if (userId != null) {
                Result.success(userId)
            } else {
                Result.failure(Exception("Kayıt başarısız"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        firebaseAuth.signOut()
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Firebase service for Firestore operations
 */
class FirebaseDataService(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun saveUserData(userId: String, userData: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(FirebaseConstants.USERS_COLLECTION)
                .document(userId)
                .set(userData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserData(userId: String): Result<Map<String, Any>?> {
        return try {
            val document = firestore.collection(FirebaseConstants.USERS_COLLECTION)
                .document(userId)
                .get()
            if (document.exists()) {
                Result.success(document.data)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncExerciseData(userId: String, exercises: List<Map<String, Any>>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            exercises.forEach { exercise ->
                val docRef = firestore.collection(FirebaseConstants.EXERCISES_COLLECTION)
                    .document()
                batch.set(docRef, exercise)
            }
            batch.commit()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncSessionData(userId: String, sessionData: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(FirebaseConstants.SESSIONS_COLLECTION)
                .add(sessionData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}