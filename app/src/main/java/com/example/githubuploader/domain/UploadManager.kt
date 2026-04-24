package com.example.githubuploader.domain

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.documentfile.provider.DocumentFile
import com.example.githubuploader.data.local.PreferencesManager
import com.example.githubuploader.data.model.CreateRepoRequest
import com.example.githubuploader.data.model.FileUpdateRequest
import com.example.githubuploader.data.remote.GitHubRepository
import com.example.githubuploader.data.remote.TokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UploadManager(
    private val context: Context,
    private val preferences: PreferencesManager
) {
    private val gitHubRepo = GitHubRepository()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    data class UploadLog(val timestamp: String, val message: String, val isError: Boolean = false)

    suspend fun createRepoAndSwitchMode(
        name: String,
        description: String,
        isPrivate: Boolean,
        onLog: (UploadLog) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onLog(UploadLog(getTimestamp(), "Creating repository: $name"))
            
            val request = CreateRepoRequest(name, description, isPrivate)
            val repo = gitHubRepo.createRepository(request).getOrThrow()
            
            preferences.createNew = false
            preferences.repoUrl = repo.htmlUrl
            
            onLog(UploadLog(getTimestamp(), "Repository created: ${repo.htmlUrl}"))
            Result.success(repo.htmlUrl)
        } catch (e: Exception) {
            onLog(UploadLog(getTimestamp(), "Failed to create repository: ${e.message}", true))
            Result.failure(e)
        }
    }

    suspend fun uploadFilesSequence(
        owner: String,
        repo: String,
        branch: String,
        ymlFiles: List<Uri>,
        zipFile: File?,
        uploadUnpack: Boolean,
        uploadBuild: Boolean,
        onLog: (UploadLog) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onLog(UploadLog(getTimestamp(), "Starting upload sequence..."))
            
            if (uploadUnpack) {
                onLog(UploadLog(getTimestamp(), "Uploading unpack.yml..."))
                val unpackContent = TemplateGenerator.generateUnpackYml()
                uploadFile(owner, repo, ".github/workflows/unpack.yml", unpackContent, branch, null)
                onLog(UploadLog(getTimestamp(), "unpack.yml uploaded successfully"))
            }
            
            if (uploadBuild) {
                onLog(UploadLog(getTimestamp(), "Uploading build.yml..."))
                val buildContent = TemplateGenerator.generateBuildYml(
                    preferences.buildBranch,
                    preferences.javaVersion,
                    preferences.gradleVersion,
                    preferences.buildType
                )
                uploadFile(owner, repo, ".github/workflows/build.yml", buildContent, branch, null)
                onLog(UploadLog(getTimestamp(), "build.yml uploaded successfully"))
            }
            
            for ((index, ymlUri) in ymlFiles.withIndex()) {
                onLog(UploadLog(getTimestamp(), "Uploading YAML file ${index + 1}/${ymlFiles.size}..."))
                val content = readFileContent(context, ymlUri)
                val fileName = getFileName(context, ymlUri) ?: "workflow${index + 1}.yml"
                uploadFile(owner, repo, ".github/workflows/$fileName", content, branch, null)
                onLog(UploadLog(getTimestamp(), "$fileName uploaded successfully"))
            }
            
            if (zipFile != null) {
                onLog(UploadLog(getTimestamp(), "Uploading ZIP file..."))
                val zipContent = zipFile.readBytes()
                val zipBase64 = Base64.encodeToString(zipContent, Base64.NO_WRAP)
                uploadFileBase64(owner, repo, zipFile.name, zipBase64, branch, null)
                onLog(UploadLog(getTimestamp(), "ZIP file uploaded successfully"))
            }
            
            onLog(UploadLog(getTimestamp(), "All files uploaded successfully!"))
            Result.success(Unit)
        } catch (e: Exception) {
            onLog(UploadLog(getTimestamp(), "Upload failed: ${e.message}", true))
            Result.failure(e)
        }
    }

    private suspend fun uploadFile(
        owner: String,
        repo: String,
        path: String,
        content: String,
        branch: String,
        sha: String?
    ) {
        val base64Content = Base64.encodeToString(content.toByteArray(), Base64.NO_WRAP)
        uploadFileBase64(owner, repo, path, base64Content, branch, sha)
    }

    private suspend fun uploadFileBase64(
        owner: String,
        repo: String,
        path: String,
        content: String,
        branch: String,
        sha: String?
    ) {
        val request = FileUpdateRequest(
            message = "Update $path",
            content = content,
            branch = branch,
            sha = sha
        )
        gitHubRepo.createOrUpdateFile(owner, repo, path, request).getOrThrow()
    }

    private fun readFileContent(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use {
            it.bufferedReader().readText()
        } ?: ""
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        return DocumentFile.fromSingleUri(context, uri)?.name
    }

    private fun getTimestamp(): String = dateFormat.format(Date())

    fun setToken(token: String) {
        TokenProvider.setToken(token)
    }
}
