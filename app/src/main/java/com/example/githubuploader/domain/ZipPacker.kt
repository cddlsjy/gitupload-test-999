package com.example.githubuploader.domain

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipPacker {
    private val PROTECTED_FILES = setOf(
        ".gitignore", "build.gradle", "settings.gradle", "gradle.properties",
        "gradlew", "gradlew.bat"
    )

    fun packFolder(
        context: Context,
        folderUri: Uri,
        excludePatterns: List<String>,
        zipFileName: String = "archive.zip"
    ): File? {
        val cacheDir = context.cacheDir
        val zipFile = File(cacheDir, zipFileName)
        
        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                val rootDoc = DocumentFile.fromTreeUri(context, folderUri) ?: return null
                val basePath = rootDoc.uri.path ?: ""
                
                addFolderToZip(context, rootDoc, basePath, excludePatterns, zos)
            }
            return zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun addFolderToZip(
        context: Context,
        docFile: DocumentFile,
        basePath: String,
        excludePatterns: List<String>,
        zos: ZipOutputStream
    ) {
        val files = docFile.listFiles()
        
        for (file in files) {
            val filePath = file.uri.path ?: continue
            val relativePath = filePath.removePrefix(basePath).removePrefix("/")
            
            if (shouldExclude(relativePath, excludePatterns, file.name ?: "")) {
                continue
            }
            
            if (file.isDirectory) {
                addFolderToZip(context, file, basePath, excludePatterns, zos)
            } else {
                addFileToZip(context, file, relativePath, zos)
            }
        }
    }

    private fun addFileToZip(
        context: Context,
        docFile: DocumentFile,
        entryPath: String,
        zos: ZipOutputStream
    ) {
        try {
            val inputStream = context.contentResolver.openInputStream(docFile.uri) ?: return
            ZipEntry(entryPath).let { entry ->
                zos.putNextEntry(entry)
                inputStream.copyTo(zos)
                zos.closeEntry()
            }
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shouldExclude(path: String, patterns: List<String>, fileName: String): Boolean {
        if (PROTECTED_FILES.contains(fileName)) {
            return false
        }
        
        for (pattern in patterns) {
            if (matchesWildcard(path, pattern)) {
                return true
            }
        }
        return false
    }

    private fun matchesWildcard(path: String, pattern: String): Boolean {
        val regexPattern = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
        
        return Regex(regexPattern).containsMatchIn(path)
    }
}
