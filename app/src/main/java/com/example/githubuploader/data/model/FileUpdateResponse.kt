package com.example.githubuploader.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FileUpdateResponse(
    val content: ContentInfo?,
    val commit: CommitInfo?
) {
    @JsonClass(generateAdapter = true)
    data class ContentInfo(
        val name: String,
        val path: String,
        val sha: String
    )

    @JsonClass(generateAdapter = true)
    data class CommitInfo(
        val sha: String,
        val message: String
    )
}
