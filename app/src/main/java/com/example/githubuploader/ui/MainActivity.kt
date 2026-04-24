package com.example.githubuploader.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.githubuploader.ui.navigation.Screen
import com.example.githubuploader.ui.screens.UploadScreen
import com.example.githubuploader.ui.screens.RepoContentScreen
import com.example.githubuploader.ui.screens.SnippetsScreen
import com.example.githubuploader.ui.screens.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GitHubUploaderTheme {
                GitHubUploaderApp()
            }
        }
    }
}

@Composable
fun GitHubUploaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}

@Composable
fun GitHubUploaderApp() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index, route ->
                    selectedTab = index
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Upload.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Upload.route) { UploadScreen() }
            composable(Screen.RepoContent.route) { RepoContentScreen() }
            composable(Screen.Snippets.route) { SnippetsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int, String) -> Unit
) {
    val tabs = listOf(
        Screen.Upload to Icons.Default.Add,
        Screen.RepoContent to Icons.Default.MoreVert,
        Screen.Snippets to Icons.Default.Edit,
        Screen.Settings to Icons.Default.Settings
    )
    val labels = listOf("上传", "仓库", "片段", "设置")

    NavigationBar {
        tabs.forEachIndexed { index, (screen, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = labels[index]) },
                label = { Text(labels[index]) },
                selected = selectedTab == index,
                onClick = { onTabSelected(index, screen.route) }
            )
        }
    }
}
