package com.example.pathtrackerapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pathtrackerapp.service.TrackingForegroundService
import com.example.pathtrackerapp.ui.navigation.NavigationWrapper
import com.example.pathtrackerapp.ui.navigation.TrackingSessionSummary
import com.example.pathtrackerapp.ui.theme.PathTrackerAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PathTrackerAppTheme(dynamicColor = false) {
                navController = rememberNavController()
                NavigationWrapper(navController = navController)
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == TrackingForegroundService.ACTION_SHOW_SUMMARY) {

            val encodedSummary = intent.getStringExtra(TrackingForegroundService.EXTRA_TRACKING_SUMMARY) ?: return

            navController.navigate(TrackingSessionSummary(summarySession = encodedSummary))
        }
    }
}