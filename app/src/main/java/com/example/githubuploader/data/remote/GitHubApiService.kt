package com.example.githubuploader.data.remote

import com.example.githubuploader.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GitHubApiService {
    @GET("user")
    suspend fun getAuthenticatedUser(): GitHubUser

    @GET("user/repos")
    suspend fun listUserRepos(
        @Query("per_page") perPage: Int = 100,
        @Query("sort") sort: String = "updated"
    ): List<GitHubRepo>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getRepoContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String = "",
        @Query("ref") branch: String
    ): List<RepoContentItem>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileDetails(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Query("ref") branch: String
    ): RepoContentItem?

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun createOrUpdateFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body body: FileUpdateRequest
    ): FileUpdateResponse

    @POST("user/repos")
    suspend fun createRepository(
        @Body body: CreateRepoRequest
    ): GitHubRepo

    @GET("repos/{owner}/{repo}/zipball/{branch}")
    @Streaming
    suspend fun downloadRepoZip(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String
    ): Response<ResponseBody>
}
