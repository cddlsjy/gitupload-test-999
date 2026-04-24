package com.example.githubuploader.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRepo(
    val id: Long,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "html_url") val htmlUrl: String,
    val private: Boolean,
    val description: String?,
    val name: String,
    val owner: Owner
) {
    @JsonClass(generateAdapter = true)
    data class Owner(
        val login: String
    )
}
