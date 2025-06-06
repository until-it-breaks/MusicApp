package com.musicapp.playback

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import coil.ImageLoader
import coil.request.ImageRequest
import com.musicapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap

@UnstableApi
class PlaybackDescriptionAdapter(
    private val context: Context,
    private val mediaPlayerManager: MediaPlayerManager,
    private val serviceScope: CoroutineScope,
    private val notificationActivityPendingIntent: PendingIntent
) : PlayerNotificationManager.MediaDescriptionAdapter {

    companion object {
        private const val NOTIFICATION_ICON_SIZE_PX = 96
    }

    override fun getCurrentContentTitle(player: Player): CharSequence {
        return mediaPlayerManager.playbackState.value.currentQueueItem?.track?.title ?: "Unknown Title"
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return mediaPlayerManager.playbackState.value.currentQueueItem?.track?.contributors?.joinToString { it.name }
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return notificationActivityPendingIntent
    }


    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        val currentTrack = mediaPlayerManager.playbackState.value.currentQueueItem?.track
        val defaultBitmap = getDefaultArtwork(context)

        currentTrack?.bigPictureUri.let { uri ->
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val imageRequest = ImageRequest.Builder(context)
                        .data(uri)
                        .size(NOTIFICATION_ICON_SIZE_PX, NOTIFICATION_ICON_SIZE_PX)
                        .allowHardware(false)
                        .build()
                    val drawable = ImageLoader(context).execute(imageRequest).drawable
                    val loadedBitmap = drawable?.toBitmap()

                    if (loadedBitmap != null) {
                        callback.onBitmap(loadedBitmap)
                    } else {
                        Log.e(
                            "PlaybackDescAdapter",
                            "Loaded drawable was null or couldn't be converted to bitmap."
                        )
                        callback.onBitmap(defaultBitmap)
                    }
                } catch (e: Exception) {
                    Log.e(
                        "PlaybackDescAdapter",
                        "Error loading album art for notification: ${e.message}"
                    )
                    callback.onBitmap(defaultBitmap)
                }
            }
        }
        return null
    }

    private fun getDefaultArtwork(context: Context): Bitmap {
        val drawable = ContextCompat.getDrawable(
            context,
            R.drawable.ic_music_note
        ) // Icon TODO change to app icon?
            ?: ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)

        val bitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val width = drawable?.intrinsicWidth ?: NOTIFICATION_ICON_SIZE_PX
            val height = drawable?.intrinsicHeight ?: NOTIFICATION_ICON_SIZE_PX
            val newBitmap = createBitmap(width, height)
            val canvas = Canvas(newBitmap)
            drawable?.setBounds(0, 0, canvas.width, canvas.height)
            drawable?.draw(canvas)
            newBitmap
        }
        return bitmap ?: createBitmap(NOTIFICATION_ICON_SIZE_PX, NOTIFICATION_ICON_SIZE_PX)
    }

}