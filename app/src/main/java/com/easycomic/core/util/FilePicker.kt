package com.easycomic.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilePicker @Inject constructor() {
    
    fun registerFilePicker(
        activity: FragmentActivity,
        onFileSelected: (Uri) -> Unit
    ): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { onFileSelected(it) }
        }
    }
    
    fun registerFilePicker(
        fragment: Fragment,
        onFileSelected: (Uri) -> Unit
    ): ActivityResultLauncher<String> {
        return fragment.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { onFileSelected(it) }
        }
    }
    
    fun launchFilePicker(launcher: ActivityResultLauncher<String>) {
        launcher.launch("*/*")
    }
    
    fun launchComicFilePicker(launcher: ActivityResultLauncher<String>) {
        val mimeTypes = arrayOf(
            "application/zip",
            "application/rar",
            "application/x-cbz",
            "application/x-cbr",
            "application/octet-stream"
        )
        launcher.launch("*/*")
    }
    
    fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        
        when (uri.scheme) {
            "content" -> {
                val cursor = context.contentResolver.query(
                    uri,
                    null,
                    null,
                    null,
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayName = it.getColumnIndex(
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME
                        )
                        if (displayName != -1) {
                            fileName = it.getString(displayName)
                        }
                    }
                }
            }
            "file" -> {
                fileName = uri.path?.substringAfterLast('/')
            }
        }
        
        return fileName
    }
    
    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                parcelFileDescriptor.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    suspend fun copyFileToInternalStorage(
        context: Context,
        uri: Uri,
        destinationDir: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val fileName = getFileName(context, uri) ?: "comic_${System.currentTimeMillis()}"
            val destinationFile = File(destinationDir, fileName)
            
            // Create destination directory if it doesn't exist
            if (!destinationDir.exists()) {
                destinationDir.mkdirs()
            }
            
            // Copy file
            context.contentResolver.openInputStream(uri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open input stream"))
            
            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isValidComicFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in listOf("zip", "rar", "cbz", "cbr")
    }
    
    fun isImageFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }
    
    class ComicFilePicker(
        private val fragment: Fragment,
        private val onFilePicked: (Uri) -> Unit
    ) {
        private val picker = fragment.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { onFilePicked(it) }
        }
        
        fun pickFile() {
            picker.launch("*/*")
        }
        
        fun pickComicFile() {
            picker.launch("*/*")
        }
    }
}