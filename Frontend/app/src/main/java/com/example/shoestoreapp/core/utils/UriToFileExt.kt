package com.example.shoestoreapp.core.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

fun Context.uriToTempFile(uri: Uri): File {
    val resolver = contentResolver
    val mimeType = resolver.getType(uri)
    val extension = MimeTypeMap.getSingleton()
        .getExtensionFromMimeType(mimeType)
        ?.let { ".${it}" }
        ?: ".jpg"

    val imagesDir = File(cacheDir, "images").apply { mkdirs() }
    val tempFile = File.createTempFile("upload_", extension, imagesDir)

    resolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    } ?: throw IllegalStateException("Unable to open input stream for Uri")

    return tempFile
}

