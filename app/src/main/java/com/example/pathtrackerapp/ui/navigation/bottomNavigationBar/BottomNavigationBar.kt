package com.example.pathtrackerapp.ui.navigation.bottomNavigationBar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.example.pathtrackerapp.ui.navigation.MainDestinations
import com.example.pathtrackerapp.R

data class BottomNavBarItem(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int?,
    val route: MainDestinations
)

object BottomBarConstants {
    val BottomNavItems = listOf(
        BottomNavBarItem(
            label = R.string.tracking,
            icon = R.drawable.ic_steps,
            contentDescription = R.string.content_description_tab_tracking,
            route = MainDestinations.Tracking
        ),
        BottomNavBarItem(
            label = R.string.log,
            icon = R.drawable.ic_log,
            contentDescription = R.string.content_description_tab_log,
            route = MainDestinations.SessionLogs
        )
    )
}

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    currentDestination: NavDestination?,
) {
    NavigationBar(modifier = modifier) {
        BottomBarConstants.BottomNavItems.forEach { tab ->
            val isSelected =
                currentDestination?.hierarchy?.any { it.route == tab.route::class.qualifiedName } == true

            NavigationBarItem(
                selected = isSelected,
                icon = {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = tab.contentDescription?.let { stringResource(it) }
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.label),
                        style = if (isSelected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall
                    )
                },
                onClick = {
                    navController.navigate(tab.route) {
                        launchSingleTop = true
                        restoreState = true

                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }
    }
}
