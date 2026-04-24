package com.example.githubuploader.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RepoContentItem(
    val name: String,
    val path: String,
    val type: String,
    val size: Int?,
    @Json(name = "download_url") val downloadUrl: String?,
    val sha: String?
)
