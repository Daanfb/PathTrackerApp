package com.example.pathtrackerapp.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pathtrackerapp.ui.navigation.bottomNavigationBar.BottomNavigationBar
import com.example.pathtrackerapp.ui.screens.sessionslog.SessionsLogScreen
import com.example.pathtrackerapp.ui.screens.tracking.TrackingScreen
import com.example.pathtrackerapp.ui.screens.trackingsummary.TrackingSummaryScreen

@Composable
fun NavigationWrapper(navController: NavHostController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = when (currentDestination?.route) {
        MainDestinations.Tracking::class.qualifiedName,
        MainDestinations.SessionLogs::class.qualifiedName -> true

        else -> false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { paddingValues ->

//        val commonModifier = Modifier
//            .padding(bottom = paddingValues.calculateBottomPadding())
//            .consumeWindowInsets(paddingValues)

        NavHost(
            navController = navController,
            startDestination = MainDestinations.Tracking,
        ) {

            composable<MainDestinations.Tracking> {
                TrackingScreen(
                    modifier = Modifier
                        .padding(bottom = paddingValues.calculateBottomPadding())
//                        .consumeWindowInsets(paddingValues)
                )
            }

            composable<MainDestinations.SessionLogs> {
                SessionsLogScreen(modifier = Modifier.padding(paddingValues))
            }

            composable<TrackingSessionSummary>(
                enterTransition = {
                    slideInVertically(
                        animationSpec = tween(500),
                        initialOffsetY = { it })
                },
                exitTransition = {
                    slideOutVertically(
                        animationSpec = tween(500),
                        targetOffsetY = { it }
                    )
                }
            ) {
                TrackingSummaryScreen(navToSessionsLog = {
                    navController.navigate(MainDestinations.SessionLogs) {
                        popUpTo(MainDestinations.Tracking) {

                            // To avoid that the user can go back to the summary screen
                            inclusive = true
                        }
                    }
                }, navBack = {
                    navController.popBackStack()
                })
            }
        }
    }
}
