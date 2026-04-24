package com.example.githubuploader.data.remote

import com.example.githubuploader.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response

class GitHubRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getAuthenticatedUser(): Result<GitHubUser> {
        return try {
            Result.success(apiService.getAuthenticatedUser())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listUserRepos(): Result<List<GitHubRepo>> {
        return try {
            Result.success(apiService.listUserRepos())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRepoContents(owner: String, repo: String, path: String, branch: String): Result<List<RepoContentItem>> {
        return try {
            Result.success(apiService.getRepoContents(owner, repo, path, branch))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFileDetails(owner: String, repo: String, path: String, branch: String): Result<RepoContentItem?> {
        return try {
            Result.success(apiService.getFileDetails(owner, repo, path, branch))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrUpdateFile(
        owner: String,
        repo: String,
        path: String,
        body: FileUpdateRequest
    ): Result<FileUpdateResponse> {
        return try {
            Result.success(apiService.createOrUpdateFile(owner, repo, path, body))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRepository(body: CreateRepoRequest): Result<GitHubRepo> {
        return try {
            Result.success(apiService.createRepository(body))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadRepoZip(owner: String, repo: String, branch: String): Result<Response<ResponseBody>> {
        return try {
            Result.success(apiService.downloadRepoZip(owner, repo, branch))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseRepoUrl(repoUrl: String): Pair<String, String>? {
        val patterns = listOf(
            Regex("""https?://github\.com/([^/]+)/([^/.]+)(\.git)?"""),
            Regex("""git@github\.com:([^/]+)/([^/.]+)(\.git)?""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(repoUrl)
            if (match != null && match.groupValues.size >= 3) {
                return Pair(match.groupValues[1], match.groupValues[2])
            }
        }
        return null
    }
}
