package com.example.my_app.data.database

import androidx.room.*
import com.example.my_app.data.model.Exercise
import com.example.my_app.data.model.ExerciseCategory
import com.example.my_app.data.model.ExerciseDifficulty
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>
    
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: String): Exercise?
    
    @Query("SELECT * FROM exercises WHERE isAssigned = 1")
    fun getAssignedExercises(): Flow<List<Exercise>>
    
    @Query("SELECT * FROM exercises WHERE category = :category")
    fun getExercisesByCategory(category: ExerciseCategory): Flow<List<Exercise>>
    
    @Query("SELECT * FROM exercises WHERE difficulty = :difficulty")
    fun getExercisesByDifficulty(difficulty: ExerciseDifficulty): Flow<List<Exercise>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)
    
    @Update
    suspend fun updateExercise(exercise: Exercise)
    
    @Delete
    suspend fun deleteExercise(exercise: Exercise)
    
    @Query("UPDATE exercises SET isAssigned = :isAssigned WHERE id = :exerciseId")
    suspend fun updateAssignmentStatus(exerciseId: String, isAssigned: Boolean)
    
    @Query("DELETE FROM exercises")
    suspend fun clearExercises()
    
    @Query("SELECT * FROM exercises WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchExercises(query: String): Flow<List<Exercise>>
}