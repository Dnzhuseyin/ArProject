package com.example.my_app.data.repository

import com.example.my_app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

data class AuthResult(
    val data: FirebaseUser?,
    val errorMessage: String?
)

data class SignUpData(
    val email: String,
    val password: String,
    val username: String,
    val fullName: String
)

data class ProfileSetupData(
    val isAthlete: Boolean,
    val hasSurgery: Boolean,
    val surgeryDetails: String,
    val doctorName: String
)

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) {
    
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    suspend fun signUpWithEmailAndPassword(signUpData: SignUpData): Result<AuthResult> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(
                signUpData.email,
                signUpData.password
            ).await()
            
            // Create user profile in Firestore
            result.user?.let { firebaseUser ->
                val user = User(
                    id = firebaseUser.uid,
                    email = signUpData.email,
                    username = signUpData.username,
                    fullName = signUpData.fullName,
                    joinDate = Date(),
                    lastActive = Date()
                )
                
                userRepository.createUser(user)
            }
            
            Result.success(AuthResult(result.user, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthResult> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(AuthResult(result.user, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<AuthResult> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            
            // Check if this is a new user and create profile
            result.user?.let { firebaseUser ->
                val existingUser = userRepository.getUserById(firebaseUser.uid)
                if (existingUser == null) {
                    val user = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        username = firebaseUser.displayName?.replace(" ", "_")?.lowercase() ?: "",
                        fullName = firebaseUser.displayName ?: "",
                        profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                        joinDate = Date(),
                        lastActive = Date()
                    )
                    
                    userRepository.createUser(user)
                }
            }
            
            Result.success(AuthResult(result.user, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            userRepository.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(profileData: ProfileSetupData): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val existingUser = userRepository.getUserById(currentUser.uid)
                existingUser?.let { user ->
                    val updatedUser = user.copy(
                        isAthlete = profileData.isAthlete,
                        hasSurgery = profileData.hasSurgery,
                        surgeryDetails = profileData.surgeryDetails,
                        doctorName = profileData.doctorName
                    )
                    
                    userRepository.updateUser(updatedUser)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getAuthStateFlow(): Flow<FirebaseUser?> = flow {
        firebaseAuth.addAuthStateListener { auth ->
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                emit(auth.currentUser)
            }
        }
    }
}