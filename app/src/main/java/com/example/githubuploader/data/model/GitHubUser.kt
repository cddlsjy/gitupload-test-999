package com.example.githubuploader.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubUser(
    val login: String,
    val name: String?,
    val email: String?,
    @Json(name = "avatar_url") val avatarUrl: String
)
