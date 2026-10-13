package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JoinReceiveLocationScreen
import com.example.ui.screens.LocationHistoryScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.PeopleScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShareLocationScreen
import com.example.ui.screens.TermsOfServiceScreen
import com.example.ui.screens.WelcomeScreen

@Composable
fun SafeTrackApp(
    viewModel: SafeTrackViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedback) {
        val fb = feedback
        if (fb != null) {
            snackbarHostState.showSnackbar(
                message = fb.message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedback()
        }
    }

    // Handle back button
    BackHandler(
        enabled = currentScreen != AppScreen.WELCOME && currentScreen != AppScreen.HOME
    ) {
        viewModel.navigateBack()
    }

    // Screens that display bottom navigation
    val showBottomBar = activeUser != null && currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.MAP,
        AppScreen.PEOPLE,
        AppScreen.SETTINGS
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isError = feedback?.isError == true
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError) Color(0xFFDC2626) else Color(0xFF10B981),
                    contentColor = Color.White
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF10B981),
                            selectedTextColor = Color(0xFF10B981),
                            indicatorColor = Color(0xFF10B981).copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.MAP,
                        onClick = { viewModel.navigateTo(AppScreen.MAP) },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                        label = { Text("Map") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF06B6D4),
                            selectedTextColor = Color(0xFF06B6D4),
                            indicatorColor = Color(0xFF06B6D4).copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_map")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PEOPLE,
                        onClick = { viewModel.navigateTo(AppScreen.PEOPLE) },
                        icon = { Icon(Icons.Default.People, contentDescription = "People") },
                        label = { Text("People") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF10B981),
                            selectedTextColor = Color(0xFF10B981),
                            indicatorColor = Color(0xFF10B981).copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_people")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SETTINGS,
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.WELCOME -> WelcomeScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.AUTH -> AuthScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.HOME -> HomeScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.SHARE -> ShareLocationScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.JOIN -> JoinReceiveLocationScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.MAP -> MapScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.PEOPLE -> PeopleScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.HISTORY -> LocationHistoryScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.PRIVACY_POLICY -> PrivacyPolicyScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
            AppScreen.TERMS_OF_SERVICE -> TermsOfServiceScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
        }
    }
}
