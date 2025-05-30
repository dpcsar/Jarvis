package site.jarviscopilot.jarvis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import site.jarviscopilot.jarvis.ui.screens.ChecklistScreen
import site.jarviscopilot.jarvis.ui.screens.MainScreen
import site.jarviscopilot.jarvis.ui.screens.SettingsScreen

// Navigation routes used in the app
object JarvisDestinations {
    const val MAIN_ROUTE = "main"
    const val CHECKLIST_ROUTE = "checklist/{checklistName}?resumeFromSaved={resumeFromSaved}"
    const val SETTINGS_ROUTE = "settings"

    // Helper functions to create route strings with arguments
    fun checklistRoute(checklistName: String, resumeFromSaved: Boolean = false): String =
        "checklist/$checklistName?resumeFromSaved=$resumeFromSaved"
}


// JarvisNavHost composable that sets up the navigation structure of the app
@Composable
fun JarvisNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = JarvisDestinations.MAIN_ROUTE,
        modifier = modifier
    ) {
        // Main screen
        composable(JarvisDestinations.MAIN_ROUTE) {
            MainScreen(
                onChecklistSelected = { checklist ->
                    navController.navigate(JarvisDestinations.checklistRoute(checklist))
                },
                onSettingsClick = {
                    navController.navigate(JarvisDestinations.SETTINGS_ROUTE)
                },
                onResumeChecklist = { checklist, resumeFromSaved ->
                    navController.navigate(
                        JarvisDestinations.checklistRoute(
                            checklist,
                            resumeFromSaved
                        )
                    )
                }
            )
        }

        // Checklist screen with name parameter and optional resume parameter
        composable(
            route = JarvisDestinations.CHECKLIST_ROUTE,
            arguments = listOf(
                navArgument("checklistName") {
                    type = NavType.StringType
                },
                navArgument("resumeFromSaved") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val checklistName = backStackEntry.arguments?.getString("checklistName") ?: ""
            val resumeFromSaved = backStackEntry.arguments?.getBoolean("resumeFromSaved") == true

            ChecklistScreen(
                checklistName = checklistName,
                resumeFromSaved = resumeFromSaved,
                onNavigateHome = {
                    navController.navigate(JarvisDestinations.MAIN_ROUTE) {
                        // Clear the back stack so pressing back won't return to the checklist
                        popUpTo(JarvisDestinations.MAIN_ROUTE) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Settings screen
        composable(JarvisDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}
