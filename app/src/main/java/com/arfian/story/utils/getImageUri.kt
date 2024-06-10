package com.arfian.story.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.arfian.story.BuildConfig
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
private const val DIRECTORY_NAME = "MyCamera"
private const val MIME_TYPE = "image/jpeg"

fun getImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        createImageUriQ(context, timeStamp)
    } else {
        createImageUriPreQ(context, timeStamp)
    }
}

private fun createImageUriQ(context: Context, timeStamp: String): Uri {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "$timeStamp.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE)
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/$DIRECTORY_NAME/")
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )!!
}

private fun createImageUriPreQ(context: Context, timeStamp: String): Uri {
    val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageDir = filesDir?.let { File(it, DIRECTORY_NAME) }
    imageDir?.mkdirs()
    val imageFile = File(imageDir, "$timeStamp.jpg")
    return FileProvider.getUriForFile(
        context,
        BuildConfig.APPLICATION_ID,
        imageFile
    )
}

fun openFile(context: Context, uri: Uri): InputStream? {
    val contentResolver: ContentResolver = context.contentResolver
    return try {
        contentResolver.openInputStream(uri)
    } catch (e: FileNotFoundException) {
        null
    }
}