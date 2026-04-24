package com.example.githubuploader.ui.navigation

sealed class Screen(val route: String) {
    object Upload : Screen("upload")
    object RepoContent : Screen("repo_content")
    object Snippets : Screen("snippets")
    object Settings : Screen("settings")
}
