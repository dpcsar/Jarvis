package site.jarviscopilot.jarvis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import site.jarviscopilot.jarvis.ui.screens.ChecklistDetailScreen
import site.jarviscopilot.jarvis.ui.screens.HomeScreen
import site.jarviscopilot.jarvis.ui.screens.NotificationsScreen
import site.jarviscopilot.jarvis.ui.screens.ProfileScreen
import site.jarviscopilot.jarvis.ui.screens.SettingsScreen
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModel

object JarvisDestinations {
    const val HOME_ROUTE = "home"
    const val CHECKLIST_DETAIL_ROUTE = "checklist/{listIndex}"
    const val SETTINGS_ROUTE = "settings"
    const val PROFILE_ROUTE = "profile"
    const val NOTIFICATIONS_ROUTE = "notifications"
    
    fun checklistDetailRoute(listIndex: Int): String {
        return "checklist/$listIndex"
    }
}

@Composable
fun JarvisNavGraph(
    navController: NavHostController,
    viewModel: ChecklistViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = JarvisDestinations.HOME_ROUTE
    ) {
        composable(JarvisDestinations.HOME_ROUTE) {
            HomeScreen(
                onChecklistSelected = { listIndex ->
                    navController.navigate(JarvisDestinations.checklistDetailRoute(listIndex))
                },
                viewModel = viewModel,
                onNavigateToSettings = {
                    navController.navigate(JarvisDestinations.SETTINGS_ROUTE)
                },
                onNavigateToNotifications = {
                    navController.navigate(JarvisDestinations.NOTIFICATIONS_ROUTE)
                }
            )
        }
        
        composable(
            route = JarvisDestinations.CHECKLIST_DETAIL_ROUTE
        ) { backStackEntry ->
            val listIndex = backStackEntry.arguments?.getString("listIndex")?.toIntOrNull() ?: 0
            
            ChecklistDetailScreen(
                listIndex = listIndex,
                onNavigateUp = { navController.navigateUp() },
                viewModel = viewModel
            )
        }
        
        composable(JarvisDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                navController = navController
            )
        }
        
        composable(JarvisDestinations.PROFILE_ROUTE) {
            ProfileScreen(
                navController = navController
            )
        }
        
        composable(JarvisDestinations.NOTIFICATIONS_ROUTE) {
            NotificationsScreen(
                navController = navController
            )
        }
    }
}