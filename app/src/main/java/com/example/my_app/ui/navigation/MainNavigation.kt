package com.example.my_app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.my_app.ui.screens.auth.*
import com.example.my_app.ui.screens.exercise.*
import com.example.my_app.ui.screens.exercises.ExercisesScreen
import com.example.my_app.ui.screens.home.HomeScreen
import com.example.my_app.ui.screens.leaderboard.LeaderboardScreen
import com.example.my_app.ui.screens.messages.MessagesScreen
import com.example.my_app.ui.screens.communication.ChatScreen
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
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon ?: item.icon else item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            bottomNavController.navigate(item.screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = bottomNavController)
            }
            composable(Screen.Exercises.route) {
                ExercisesScreen(navController = bottomNavController)
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen(navController = bottomNavController)
            }
            composable(Screen.Messages.route) {
                MessagesScreen(navController = bottomNavController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController = bottomNavController)
            }
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen(navController = bottomNavController)
            }
            
            // Exercise detail and session screens
            composable(
                route = Screen.ExerciseDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                ExerciseDetailScreen(
                    navController = bottomNavController,
                    exerciseId = exerciseId
                )
            }
            
            composable(
                route = Screen.ExerciseSession.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                ExerciseSessionScreen(
                    navController = bottomNavController,
                    exerciseId = exerciseId
                )
            }
            
            // Communication screens
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: "default"
                ChatScreen(
                    navController = bottomNavController,
                    conversationId = conversationId
                )
            }
        }
    }
}