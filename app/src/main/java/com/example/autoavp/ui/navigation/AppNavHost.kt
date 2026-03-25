package com.example.autoavp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autoavp.data.repository.SettingsRepository
import com.example.autoavp.ui.scan.ScanScreen
import com.example.autoavp.ui.scan.ScanViewModel
import com.example.autoavp.ui.session.SessionDetailsScreen
import com.example.autoavp.ui.office.OfficeScreen
import com.example.autoavp.ui.home.HomeScreen
import com.example.autoavp.ui.history.HistoryScreen
import com.example.autoavp.ui.preview.PrintPreviewScreen
import com.example.autoavp.ui.settings.SettingsScreen
import com.example.autoavp.ui.welcome.WelcomeScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(settingsRepository: SettingsRepository) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val hasSeenWelcome by settingsRepository.hasSeenWelcome.collectAsState()
    val startDestination = if (hasSeenWelcome)
        Screen.Home.route else Screen.Welcome.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onFinish = {
                    scope.launch { settingsRepository.setHasSeenWelcome(true) }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.Scan.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode")
            val viewModel: ScanViewModel = hiltViewModel()
            viewModel.setScanMode(mode)

            ScanScreen(
                viewModel = viewModel,
                onFinishScan = {
                    navController.popBackStack()
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onScanResult = { data ->
                    // Si on est en mode retour, on passe les données à l'écran précédent
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("scanned_tracking", data.trackingNumber)
                        set("scanned_address", data.rawText)
                        set("scanned_image_path", data.imagePath)
                        // On passe aussi les clés techniques si besoin
                        set("scanned_iso", data.isoKey)
                        set("scanned_ocr", data.ocrKey)
                        set("scanned_status", data.confidenceStatus.name)
                    }
                }
            )
        }

        composable(
            route = Screen.SessionDetails.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            SessionDetailsScreen(navController = navController)
        }

        composable(Screen.Offices.route) {
            OfficeScreen(navController = navController)
        }

        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(
            route = Screen.PrintPreview.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType },
                navArgument("officeId") { type = NavType.LongType }
            )
        ) {
            PrintPreviewScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
