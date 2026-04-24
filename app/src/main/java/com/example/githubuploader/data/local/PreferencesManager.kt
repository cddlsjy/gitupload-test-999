package com.example.githubuploader.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.githubuploader.data.model.Snippet
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val snippetListType = Types.newParameterizedType(List::class.java, Snippet::class.java)
    private val snippetAdapter = moshi.adapter<List<Snippet>>(snippetListType)

    companion object {
        private const val PREFS_NAME = "github_uploader_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_BRANCH = "branch"
        private const val KEY_REPO_URL = "repo_url"
        private const val KEY_CREATE_NEW = "create_new"
        private const val KEY_NEW_REPO_NAME = "new_repo_name"
        private const val KEY_NEW_REPO_DESC = "new_repo_desc"
        private const val KEY_NEW_REPO_PRIVATE = "new_repo_private"
        private const val KEY_UPLOAD_UNPACK = "upload_unpack"
        private const val KEY_UPLOAD_BUILD = "upload_build"
        private const val KEY_BUILD_BRANCH = "build_branch"
        private const val KEY_JAVA_VERSION = "java_version"
        private const val KEY_JAVA_HOME = "java_home"
        private const val KEY_GRADLE_VERSION = "gradle_version"
        private const val KEY_BUILD_TYPE = "build_type"
        private const val KEY_EXCLUDE_PATTERNS = "exclude_patterns"
        private const val KEY_SNIPPETS = "snippets"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_DIALOG_SCALE = "dialog_scale"
    }

    var token: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var branch: String
        get() = prefs.getString(KEY_BRANCH, "main") ?: "main"
        set(value) = prefs.edit().putString(KEY_BRANCH, value).apply()

    var repoUrl: String
        get() = prefs.getString(KEY_REPO_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_REPO_URL, value).apply()

    var createNew: Boolean
        get() = prefs.getBoolean(KEY_CREATE_NEW, false)
        set(value) = prefs.edit().putBoolean(KEY_CREATE_NEW, value).apply()

    var newRepoName: String
        get() = prefs.getString(KEY_NEW_REPO_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NEW_REPO_NAME, value).apply()

    var newRepoDesc: String
        get() = prefs.getString(KEY_NEW_REPO_DESC, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NEW_REPO_DESC, value).apply()

    var newRepoPrivate: Boolean
        get() = prefs.getBoolean(KEY_NEW_REPO_PRIVATE, true)
        set(value) = prefs.edit().putBoolean(KEY_NEW_REPO_PRIVATE, value).apply()

    var uploadUnpack: Boolean
        get() = prefs.getBoolean(KEY_UPLOAD_UNPACK, true)
        set(value) = prefs.edit().putBoolean(KEY_UPLOAD_UNPACK, value).apply()

    var uploadBuild: Boolean
        get() = prefs.getBoolean(KEY_UPLOAD_BUILD, true)
        set(value) = prefs.edit().putBoolean(KEY_UPLOAD_BUILD, value).apply()

    var buildBranch: String
        get() = prefs.getString(KEY_BUILD_BRANCH, "main") ?: "main"
        set(value) = prefs.edit().putString(KEY_BUILD_BRANCH, value).apply()

    var javaVersion: String
        get() = prefs.getString(KEY_JAVA_VERSION, "17") ?: "17"
        set(value) = prefs.edit().putString(KEY_JAVA_VERSION, value).apply()

    var javaHome: String
        get() = prefs.getString(KEY_JAVA_HOME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_JAVA_HOME, value).apply()

    var gradleVersion: String
        get() = prefs.getString(KEY_GRADLE_VERSION, "8.0") ?: "8.0"
        set(value) = prefs.edit().putString(KEY_GRADLE_VERSION, value).apply()

    var buildType: String
        get() = prefs.getString(KEY_BUILD_TYPE, "debug") ?: "debug"
        set(value) = prefs.edit().putString(KEY_BUILD_TYPE, value).apply()

    var excludePatterns: String
        get() = prefs.getString(KEY_EXCLUDE_PATTERNS, getDefaultExcludes()) ?: getDefaultExcludes()
        set(value) = prefs.edit().putString(KEY_EXCLUDE_PATTERNS, value).apply()

    var fontScale: Float
        get() = prefs.getFloat(KEY_FONT_SCALE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_FONT_SCALE, value).apply()

    var dialogScale: Float
        get() = prefs.getFloat(KEY_DIALOG_SCALE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_DIALOG_SCALE, value).apply()

    fun getSnippets(): List<Snippet> {
        val json = prefs.getString(KEY_SNIPPETS, null) ?: return getDefaultSnippets()
        return snippetAdapter.fromJson(json) ?: getDefaultSnippets()
    }

    fun saveSnippets(snippets: List<Snippet>) {
        val limitedSnippets = snippets.take(10)
        val json = snippetAdapter.toJson(limitedSnippets)
        prefs.edit().putString(KEY_SNIPPETS, json).apply()
    }

    fun updateSnippet(index: Int, name: String, content: String) {
        val snippets = getSnippets().toMutableList()
        if (index in snippets.indices) {
            snippets[index] = Snippet(name, content)
            saveSnippets(snippets)
        }
    }

    fun getDefaultExcludes(): String = """
        **/.git/**
        **/build/**
        **/.gradle/**
        **/local.properties
        **/.idea/**
        **/.DS_Store
        **/*.iml
        **/.vscode/**
    """.trimIndent()

    private fun getDefaultSnippets(): List<Snippet> = listOf(
        Snippet("README", "# Project\n\nDescription here."),
        Snippet("Gitignore", "*.iml\n.gradle\n/local.properties\n/build")
    )

    fun getExcludePatternList(): List<String> {
        return excludePatterns.lines().filter { it.isNotBlank() }
    }
}
