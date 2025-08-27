package com.example.my_app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for main application flows
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainAppIntegrationTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun testNavigationBetweenMainScreens() {
        // Test bottom navigation functionality
        
        // Start on Home screen
        composeTestRule.onNodeWithText("Ana Sayfa").assertIsDisplayed()
        
        // Navigate to Exercises
        composeTestRule.onNodeWithText("Egzersizler").performClick()
        composeTestRule.onNodeWithText("Egzersizlerim").assertIsDisplayed()
        
        // Navigate to Statistics
        composeTestRule.onNodeWithText("İstatistikler").performClick()
        composeTestRule.onNodeWithText("İstatistiklerim").assertIsDisplayed()
        
        // Navigate to Messages
        composeTestRule.onNodeWithText("Mesajlar").performClick()
        composeTestRule.onNodeWithText("Mesajlar").assertIsDisplayed()
        
        // Navigate to Profile
        composeTestRule.onNodeWithText("Profil").performClick()
        composeTestRule.onNodeWithText("Profilim").assertIsDisplayed()
    }
    
    @Test
    fun testExerciseListAndDetailNavigation() {
        // Navigate to exercises screen
        composeTestRule.onNodeWithText("Egzersizler").performClick()
        
        // Check if exercises are displayed
        composeTestRule.onNodeWithText("Egzersizlerim").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bugünün Egzersizleri").assertIsDisplayed()
        
        // Click on first exercise (if exists)
        composeTestRule.onAllNodesWithText("Detayları Gör").onFirst().performClick()
        
        // Should navigate to exercise detail
        composeTestRule.onNodeWithText("Egzersize Başla").assertIsDisplayed()
    }
    
    @Test
    fun testExerciseSessionFlow() {
        // Navigate to exercises
        composeTestRule.onNodeWithText("Egzersizler").performClick()
        
        // Click on first exercise detail
        composeTestRule.onAllNodesWithText("Detayları Gör").onFirst().performClick()
        
        // Start exercise session
        composeTestRule.onNodeWithText("Egzersize Başla").performClick()
        
        // Check if session screen is displayed
        composeTestRule.onNodeWithText("Egzersizi Tamamla").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tekrar Sayısı").assertIsDisplayed()
        composeTestRule.onNodeWithText("Set").assertIsDisplayed()
    }
    
    @Test
    fun testProfileInformationDisplay() {
        // Navigate to profile
        composeTestRule.onNodeWithText("Profil").performClick()
        
        // Check profile sections are displayed
        composeTestRule.onNodeWithText("Profilim").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kişisel Bilgiler").assertIsDisplayed()
        composeTestRule.onNodeWithText("İstatistikler").assertIsDisplayed()
        composeTestRule.onNodeWithText("Başarılar").assertIsDisplayed()
    }
    
    @Test
    fun testStatisticsScreenDisplay() {
        // Navigate to statistics
        composeTestRule.onNodeWithText("İstatistikler").performClick()
        
        // Check statistics sections
        composeTestRule.onNodeWithText("İstatistiklerim").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bu Hafta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kategori Dağılımı").assertIsDisplayed()
        composeTestRule.onNodeWithText("Egzersiz Geçmişi").assertIsDisplayed()
    }
    
    @Test
    fun testMessagingFlow() {
        // Navigate to messages
        composeTestRule.onNodeWithText("Mesajlar").performClick()
        
        // Check messages screen
        composeTestRule.onNodeWithText("Mesajlar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dr. Ahmet Yılmaz").assertIsDisplayed()
        
        // Test chat navigation
        composeTestRule.onNodeWithContentDescription("Mesaj Gönder").performClick()
        
        // Should navigate to chat screen
        composeTestRule.onNodeWithText("Dr. Ahmet Yılmaz").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Mesaj gönder").assertIsDisplayed()
    }
    
    @Test
    fun testVideoCallNavigation() {
        // Navigate to messages
        composeTestRule.onNodeWithText("Mesajlar").performClick()
        
        // Click on video call button
        composeTestRule.onNodeWithContentDescription("Görüntülü Arama").performClick()
        
        // Should navigate to video call screen
        composeTestRule.onNodeWithText("Dr. Ahmet Yılmaz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Video Arama Başlatılıyor...").assertIsDisplayed()
    }
    
    @Test
    fun testLeaderboardAccess() {
        // Navigate to home screen
        composeTestRule.onNodeWithText("Ana Sayfa").performClick()
        
        // Look for leaderboard access from home screen
        composeTestRule.onNodeWithText("Liderlik Tablosu").performClick()
        
        // Should show leaderboard
        composeTestRule.onNodeWithText("Liderlik Tablosu").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bu Hafta").assertIsDisplayed()
    }
    
    @Test
    fun testDailyTasksDisplay() {
        // Navigate to home screen
        composeTestRule.onNodeWithText("Ana Sayfa").performClick()
        
        // Check if daily tasks section is visible
        composeTestRule.onNodeWithText("Günlük Görevler").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bugün").assertIsDisplayed()
    }
    
    @Test
    fun testAchievementsDisplay() {
        // Navigate to profile
        composeTestRule.onNodeWithText("Profil").performClick()
        
        // Check achievements section
        composeTestRule.onNodeWithText("Başarılar").assertIsDisplayed()
        
        // Should show some achievements
        composeTestRule.onNodeWithText("İlk Egzersiz").assertIsDisplayed()
    }
    
    @Test
    fun testSearchFunctionality() {
        // Navigate to exercises
        composeTestRule.onNodeWithText("Egzersizler").performClick()
        
        // Look for search functionality
        composeTestRule.onNodeWithContentDescription("Ara").assertExists()
        
        // Could test search input if implemented
        // composeTestRule.onNodeWithContentDescription("Ara").performClick()
    }
}