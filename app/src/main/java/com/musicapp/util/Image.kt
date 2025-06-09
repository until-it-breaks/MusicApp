package com.musicapp.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

private const val TAG = "ImageUtil"

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, playlistId: String): Uri {
    val filename = "playlist_$playlistId.png"
    val file = File(context.filesDir, filename)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    Log.i(TAG, "Saved bitmap at: $filename")
    return Uri.fromFile(file)
}

fun deleteImageFromInternalStorage(context: Context, uriString: String) {
    try {
        val file = File(uriString.toUri().path ?: return)
        if (file.exists()) {
            file.delete()
            Log.i(TAG, "Deleted bitmap at: $file")
        }
    } catch (e: Exception) {
        Log.e(TAG, e.localizedMessage, e)
    }
}

/**
 * Creates a bitmap out of 4 image uris. It places them at each corner.
 * Returns null if there are not enough of them
 */
suspend fun combineBitmapsFromUris(context: Context, uris: List<Uri>): Bitmap? {
    val imageLoader = ImageLoader(context)
    val bitmaps = mutableListOf<Bitmap>()

    for (uri in uris) {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(Size.ORIGINAL)
            .allowHardware(false)
            .build()

        val result = imageLoader.execute(request)
        val drawable = result.drawable
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
        if (bitmap != null) {
            bitmaps.add(bitmap)
        }
    }

    if (bitmaps.size < 4) return null

    val size = 250 // Size of a single image. Final output is double that.
    val finalBitmap = createBitmap(size * 2, size * 2)
    val canvas = Canvas(finalBitmap)

    for ((index, bitmap) in bitmaps.take(4).withIndex()) {
        val resized = bitmap.scale(size, size, false)
        val x = (index % 2) * size
        val y = (index / 2) * size
        canvas.drawBitmap(resized, x.toFloat(), y.toFloat(), null)
    }
    return finalBitmap
}

fun uriToBitmap(imageUri: Uri, contentResolver: ContentResolver): Bitmap {
    val bitmap = when {
        Build.VERSION.SDK_INT < 28 -> {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }
        else -> {
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }
    }
    return bitmap
}

fun saveImageToStorage(
    imageUri: Uri,
    contentResolver: ContentResolver,
    name: String = "IMG_${SystemClock.uptimeMillis()}"
): Uri {
    val bitmap = uriToBitmap(imageUri, contentResolver)

    val values = ContentValues()
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    values.put(MediaStore.Images.Media.DISPLAY_NAME, name)

    val savedImageUri =
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    val outputStream = savedImageUri?.let { contentResolver.openOutputStream(it) }
        ?: throw FileNotFoundException()

    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()

    return savedImageUri
}
