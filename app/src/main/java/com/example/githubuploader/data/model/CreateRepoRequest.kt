package com.example.githubuploader.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateRepoRequest(
    val name: String,
    val description: String?,
    val private: Boolean,
    val auto_init: Boolean = false
)
