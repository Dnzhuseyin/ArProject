package com.example.my_app.di

import android.content.Context
import androidx.room.Room
import com.example.my_app.data.database.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "physiotherapy_database"
        ).build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
    
    @Provides
    fun provideExerciseDao(database: AppDatabase): ExerciseDao = database.exerciseDao()
    
    @Provides
    fun provideExerciseSessionDao(database: AppDatabase): ExerciseSessionDao = database.exerciseSessionDao()
    
    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()
    
    @Provides
    fun provideAchievementDao(database: AppDatabase): AchievementDao = database.achievementDao()
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}