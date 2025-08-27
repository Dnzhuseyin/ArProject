package com.example.my_app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.my_app.R
import com.example.my_app.ui.screens.auth.*
import com.example.my_app.ui.screens.exercise.*
import com.example.my_app.ui.screens.exercises.ExercisesScreen
import com.example.my_app.ui.screens.home.HomeScreen
import com.example.my_app.ui.screens.leaderboard.LeaderboardScreen
import com.example.my_app.ui.screens.messages.MessagesScreen
import com.example.my_app.ui.screens.communication.ChatScreen
import com.example.my_app.ui.screens.communication.VideoCallScreen
import com.example.my_app.ui.screens.profile.ProfileScreen
import com.example.my_app.ui.screens.statistics.StatisticsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Authentication Flow
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController = navController)
        }
        
        // Main App Flow
        composable(Screen.Home.route) {
            MainAppNavigation(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation(navController: NavHostController) {
    val bottomNavController = rememberNavController()
    
    val bottomNavItems = listOf(
        BottomNavItem(
            screen = Screen.Home,
            title = "Ana Sayfa",
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        ),
        BottomNavItem(
            screen = Screen.Exercises,
            title = "Egzersizler",
            icon = Icons.Outlined.FitnessCenter,
            selectedIcon = Icons.Filled.FitnessCenter
        ),
        BottomNavItem(
            screen = Screen.Statistics,
            title = "İstatistikler",
            icon = Icons.Outlined.BarChart,
            selectedIcon = Icons.Filled.BarChart
        ),
        BottomNavItem(
            screen = Screen.Messages,
            title = "Mesajlar",
            icon = Icons.Outlined.Message,
            selectedIcon = Icons.Filled.Message
        ),
        BottomNavItem(
            screen = Screen.Profile,
            title = "Profil",
            icon = Icons.Outlined.Person,
            selectedIcon = Icons.Filled.Person
        )
    )\n\n    Scaffold(\n        bottomBar = {\n            NavigationBar {\n                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()\n                val currentDestination = navBackStackEntry?.destination\n\n                bottomNavItems.forEach { item ->\n                    val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true\n                    \n                    NavigationBarItem(\n                        icon = {\n                            Icon(\n                                imageVector = if (selected) item.selectedIcon ?: item.icon else item.icon,\n                                contentDescription = item.title\n                            )\n                        },\n                        label = { Text(item.title) },\n                        selected = selected,\n                        onClick = {\n                            bottomNavController.navigate(item.screen.route) {\n                                popUpTo(bottomNavController.graph.findStartDestination().id) {\n                                    saveState = true\n                                }\n                                launchSingleTop = true\n                                restoreState = true\n                            }\n                        }\n                    )\n                }\n            }\n        }\n    ) { innerPadding ->\n        NavHost(\n            navController = bottomNavController,\n            startDestination = Screen.Home.route,\n            modifier = Modifier.padding(innerPadding)\n        ) {\n            composable(Screen.Home.route) {\n                HomeScreen(navController = bottomNavController)\n            }\n            composable(Screen.Exercises.route) {\n                ExercisesScreen(navController = bottomNavController)\n            }\n            composable(Screen.Statistics.route) {\n                StatisticsScreen(navController = bottomNavController)\n            }\n            composable(Screen.Messages.route) {\n                MessagesScreen(navController = bottomNavController)\n            }\n            composable(Screen.Profile.route) {\n                ProfileScreen(navController = bottomNavController)\n            }\n            composable(Screen.Leaderboard.route) {\n                LeaderboardScreen(navController = bottomNavController)\n            }\n            \n            // Exercise detail and session screens\n            composable(\n                route = Screen.ExerciseDetail.route,\n                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })\n            ) { backStackEntry ->\n                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""\n                ExerciseDetailScreen(\n                    navController = bottomNavController,\n                    exerciseId = exerciseId\n                )\n            }\n            \n            composable(\n                route = Screen.ExerciseSession.route,\n                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })\n            ) { backStackEntry ->\n                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""\n                ExerciseSessionScreen(\n                    navController = bottomNavController,\n                    exerciseId = exerciseId\n                )\n            }\n            \n            // Communication screens\n            composable(\n                route = Screen.Chat.route,\n                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })\n            ) { backStackEntry ->\n                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: "default"\n                ChatScreen(\n                    navController = bottomNavController,\n                    conversationId = conversationId\n                )\n            }\n        }\n    }\n}