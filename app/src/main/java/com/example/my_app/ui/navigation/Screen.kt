package com.example.my_app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ProfileSetup : Screen("profile_setup")
    
    // Main screens with bottom navigation
    object Home : Screen("home")
    object Exercises : Screen("exercises")
    object Statistics : Screen("statistics")
    object Messages : Screen("messages")
    object Profile : Screen("profile")
    object Leaderboard : Screen("leaderboard")
    
    // Exercise related screens
    object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_detail/$exerciseId"
    }
    object ExerciseSession : Screen("exercise_session/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_session/$exerciseId"
    }
    
    // Communication screens
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    object VideoCall : Screen("video_call/{userId}") {
        fun createRoute(userId: String) = "video_call/$userId"
    }
    
    // Settings and profile screens
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
    object Achievements : Screen("achievements")
    object DailyTasks : Screen("daily_tasks")
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
)