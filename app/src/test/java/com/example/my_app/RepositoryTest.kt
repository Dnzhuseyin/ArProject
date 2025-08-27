package com.example.my_app.data.repository

import com.example.my_app.data.database.dao.*
import com.example.my_app.data.database.entity.*
import com.example.my_app.data.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.flow.flowOf
import java.util.*

/**
 * Unit tests for UserRepository
 */
class UserRepositoryTest {
    
    @Mock
    private lateinit var userDao: UserDao
    
    private lateinit var userRepository: UserRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        userRepository = UserRepository(userDao)
    }
    
    @Test
    fun `test getUserById returns user when found`() = runTest {
        // Given
        val userId = "test-user-123"
        val userEntity = UserEntity(
            id = userId,
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            dateOfBirth = Date(),
            height = 175,
            weight = 70,
            isAthlete = false,
            hasSurgeryHistory = false,
            totalPoints = 0,
            level = 1,
            streak = 0,
            totalExercises = 0,
            completedExercises = 0
        )
        `when`(userDao.getUserById(userId)).thenReturn(userEntity)
        
        // When
        val result = userRepository.getUserById(userId)
        
        // Then
        assertNotNull(result)
        assertEquals(userId, result?.id)
        assertEquals("test@test.com", result?.email)
    }
    
    @Test
    fun `test getUserById returns null when user not found`() = runTest {
        // Given
        val userId = "non-existent-user"
        `when`(userDao.getUserById(userId)).thenReturn(null)
        
        // When
        val result = userRepository.getUserById(userId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `test insertUser calls dao insert`() = runTest {
        // Given
        val user = User(
            id = "test-user",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            dateOfBirth = Date(),
            height = 175,
            weight = 70,
            isAthlete = false,
            hasSurgeryHistory = false
        )
        
        // When
        userRepository.insertUser(user)
        
        // Then
        verify(userDao).insertUser(any())
    }
}

/**
 * Unit tests for ExerciseRepository
 */
class ExerciseRepositoryTest {
    
    @Mock
    private lateinit var exerciseDao: ExerciseDao
    
    private lateinit var exerciseRepository: ExerciseRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        exerciseRepository = ExerciseRepository(exerciseDao)
    }
    
    @Test
    fun `test getAllExercises returns list of exercises`() = runTest {
        // Given
        val exerciseEntities = listOf(
            ExerciseEntity(
                id = "ex1",
                title = "Push Up",
                description = "Basic push up exercise",
                category = "Strength",
                difficulty = "Beginner",
                duration = 300,
                repetitions = 10,
                sets = 3,
                pointValue = 10,
                videoUrl = "",
                thumbnailUrl = "",
                instructions = listOf("Step 1", "Step 2"),
                targetMuscles = listOf("Chest", "Arms"),
                equipment = emptyList(),
                isAssigned = true,
                completionCount = 0,
                bestTime = 0
            )
        )
        `when`(exerciseDao.getAllExercises()).thenReturn(flowOf(exerciseEntities))
        
        // When
        val result = exerciseRepository.getAllExercises()
        
        // Then
        result.collect { exercises ->
            assertEquals(1, exercises.size)
            assertEquals("Push Up", exercises[0].title)
            assertEquals("Strength", exercises[0].category)
        }
    }
    
    @Test
    fun `test getExerciseById returns correct exercise`() = runTest {
        // Given
        val exerciseId = "ex1"
        val exerciseEntity = ExerciseEntity(
            id = exerciseId,
            title = "Push Up",
            description = "Basic push up exercise",
            category = "Strength",
            difficulty = "Beginner",
            duration = 300,
            repetitions = 10,
            sets = 3,
            pointValue = 10,
            videoUrl = "",
            thumbnailUrl = "",
            instructions = listOf("Step 1", "Step 2"),
            targetMuscles = listOf("Chest", "Arms"),
            equipment = emptyList(),
            isAssigned = true,
            completionCount = 0,
            bestTime = 0
        )
        `when`(exerciseDao.getExerciseById(exerciseId)).thenReturn(exerciseEntity)
        
        // When
        val result = exerciseRepository.getExerciseById(exerciseId)
        
        // Then
        assertNotNull(result)
        assertEquals("Push Up", result?.title)
        assertEquals("Strength", result?.category)
    }
}

/**
 * Unit tests for SessionRepository
 */
class SessionRepositoryTest {
    
    @Mock
    private lateinit var sessionDao: SessionDao
    
    private lateinit var sessionRepository: SessionRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        sessionRepository = SessionRepository(sessionDao)
    }
    
    @Test
    fun `test insertSession calls dao insert`() = runTest {
        // Given
        val session = ExerciseSession(
            id = "session1",
            userId = "user1",
            exerciseId = "ex1",
            startTime = Date(),
            endTime = Date(),
            duration = 300,
            completedReps = 10,
            completedSets = 3,
            pointsEarned = 10,
            isCompleted = true,
            averageHeartRate = 120,
            caloriesBurned = 50,
            notes = ""
        )
        
        // When
        sessionRepository.insertSession(session)
        
        // Then
        verify(sessionDao).insertSession(any())
    }
    
    @Test
    fun `test getSessionsByUserId returns user sessions`() = runTest {
        // Given
        val userId = "user1"
        val sessionEntities = listOf(
            SessionEntity(
                id = "session1",
                userId = userId,
                exerciseId = "ex1",
                startTime = Date(),
                endTime = Date(),
                duration = 300,
                completedReps = 10,
                completedSets = 3,
                pointsEarned = 10,
                isCompleted = true,
                averageHeartRate = 120,
                caloriesBurned = 50,
                notes = ""
            )
        )
        `when`(sessionDao.getSessionsByUserId(userId)).thenReturn(flowOf(sessionEntities))
        
        // When
        val result = sessionRepository.getSessionsByUserId(userId)
        
        // Then
        result.collect { sessions ->
            assertEquals(1, sessions.size)
            assertEquals(userId, sessions[0].userId)
            assertTrue(sessions[0].isCompleted)
        }
    }
}

/**
 * Unit tests for Data Model conversions
 */
class DataModelTest {
    
    @Test
    fun `test User to UserEntity conversion`() {
        // Given
        val user = User(
            id = "test-user",
            email = "test@test.com",
            firstName = "Test",
            lastName = "User",
            dateOfBirth = Date(),
            height = 175,
            weight = 70,
            isAthlete = true,
            hasSurgeryHistory = false
        )
        
        // When
        val userEntity = user.toEntity()
        
        // Then
        assertEquals(user.id, userEntity.id)
        assertEquals(user.email, userEntity.email)
        assertEquals(user.firstName, userEntity.firstName)
        assertEquals(user.lastName, userEntity.lastName)
        assertEquals(user.isAthlete, userEntity.isAthlete)
        assertEquals(user.hasSurgeryHistory, userEntity.hasSurgeryHistory)
    }
    
    @Test
    fun `test Exercise to ExerciseEntity conversion`() {
        // Given
        val exercise = Exercise(
            id = "ex1",
            title = "Push Up",
            description = "Basic push up",
            category = "Strength",
            difficulty = "Beginner",
            duration = 300,
            repetitions = 10,
            sets = 3,
            pointValue = 10,
            videoUrl = "https://example.com/video.mp4",
            thumbnailUrl = "https://example.com/thumb.jpg",
            instructions = listOf("Step 1", "Step 2"),
            targetMuscles = listOf("Chest"),
            equipment = emptyList(),
            isAssigned = true
        )
        
        // When
        val exerciseEntity = exercise.toEntity()
        
        // Then
        assertEquals(exercise.id, exerciseEntity.id)
        assertEquals(exercise.title, exerciseEntity.title)
        assertEquals(exercise.category, exerciseEntity.category)
        assertEquals(exercise.difficulty, exerciseEntity.difficulty)
        assertEquals(exercise.isAssigned, exerciseEntity.isAssigned)
    }
    
    @Test
    fun `test points calculation for completed exercise`() {
        // Given
        val exercise = Exercise(
            id = "ex1",
            title = "Push Up",
            description = "Basic push up",
            category = "Strength",
            difficulty = "Beginner",
            duration = 300,
            repetitions = 10,
            sets = 3,
            pointValue = 10,
            videoUrl = "",
            thumbnailUrl = "",
            instructions = emptyList(),
            targetMuscles = emptyList(),
            equipment = emptyList(),
            isAssigned = true
        )
        
        val completedSets = 3
        val targetSets = 3
        
        // When
        val basePoints = exercise.pointValue
        val bonusPoints = if (completedSets >= targetSets) 5 else 0
        val totalPoints = basePoints + bonusPoints
        
        // Then
        assertEquals(10, basePoints)
        assertEquals(5, bonusPoints)
        assertEquals(15, totalPoints)
    }
}