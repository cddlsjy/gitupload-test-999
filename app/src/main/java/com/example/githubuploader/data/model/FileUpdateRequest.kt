package com.example.githubuploader.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FileUpdateRequest(
    val message: String,
    val content: String,
    val branch: String,
    val sha: String? = null
)
