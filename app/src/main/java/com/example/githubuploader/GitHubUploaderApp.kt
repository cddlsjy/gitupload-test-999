package com.example.githubuploader

import android.app.Application
import com.example.githubuploader.data.local.PreferencesManager
import com.example.githubuploader.data.remote.TokenProvider

class GitHubUploaderApp : Application() {
    lateinit var preferences: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = PreferencesManager(this)
        TokenProvider.setToken(preferences.token)
    }
}
