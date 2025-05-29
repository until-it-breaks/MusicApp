package com.musicapp.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.musicapp.R
import com.musicapp.MainActivity
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import androidx.core.graphics.createBitmap

class MediaPlaybackService : Service() {

    private val mediaPlayerManager: MediaPlayerManager by inject() // Inject your MediaPlayerManager
    private var mediaSession: MediaSessionCompat? = null
    private var notificationManager: NotificationManager? = null

    // Coroutine scope for observing playback state
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "music_playback_channel"
        const val CHANNEL_NAME = "Music Playback"

        // Actions for MediaButtonReceiver
        const val ACTION_PLAY_PAUSE = "com.musicapp.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.musicapp.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.musicapp.ACTION_PREVIOUS"
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        // Create a MediaSession
        mediaSession = MediaSessionCompat(baseContext, "MusicAppMediaSession").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS // Deprecated in java
            )
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    mediaPlayerManager.resume()
                }

                override fun onPause() {
                    Log.d("MediaPlaybackService", "MediaSession: onPause")
                    mediaPlayerManager.pause()
                }

                override fun onSkipToNext() {
                    Log.d("MediaPlaybackService", "MediaSession: onSkipToNext")
                    mediaPlayerManager.playNext()
                }

                override fun onSkipToPrevious() {
                    Log.d("MediaPlaybackService", "MediaSession: onSkipToPrevious")
                    mediaPlayerManager.playPrevious()
                }

                override fun onStop() {
                    Log.d("MediaPlaybackService", "MediaSession: onStop")
                    mediaPlayerManager.stop()
                    stopSelf() // Stop the service when playback stops
                }
            })
            isActive = true
        }

        serviceScope.launch {
            mediaPlayerManager.playbackState.collectLatest { uiState ->
                Log.d("MediaPlaybackService", "PlaybackUiState updated: $uiState")
                if (uiState.currentTrack != null) {
                    updateMediaSessionMetadata(uiState.currentTrack)
                    updateMediaSessionPlaybackState(uiState)
                    buildNotification(uiState)
                } else {
                    // If no track is playing remove notification
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    mediaSession?.isActive = false
                    mediaSession?.setMetadata(null)
                    mediaSession?.setPlaybackState(null)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            // Handle media button presses (e.g., from Bluetooth headphones)
            MediaButtonReceiver.handleIntent(mediaSession, intent)
        }
        return START_STICKY // Service will be restarted if killed by system
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not binding, just running as a started service
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MediaPlaybackService", "MediaPlaybackService destroyed.")
        serviceJob.cancel() // Cancel all coroutines
        mediaSession?.release() // Release MediaSession resources
        stopForeground(STOP_FOREGROUND_REMOVE) // Ensure notification is removed
        // You might want to also call mediaPlayerManager.release() here if
        // MediaPlayerManager's lifecycle is tied to this service.
        // If MediaPlayerManager is an application-scoped singleton, then it's released by Koin later.
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low importance so it doesn't interrupt, but is persistent
            ).apply {
                description = "Notification for music playback control"
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun updateMediaSessionMetadata(track: TrackModel) {
        serviceScope.launch(Dispatchers.IO) { // Use IO dispatcher for image loading
            val albumArt: Bitmap? = try {
                val imageRequest = ImageRequest.Builder(baseContext)
                    .data(track.bigPictureUri)
                    .size(NOTIFICATION_ICON_SIZE)
                    .build()
                ImageLoader(baseContext).execute(imageRequest).drawable?.toBitmap()
            } catch (e: Exception) {
                Log.e("MediaPlaybackService", "Error loading album art for notification: ${e.message}")
                null
            }

            val artists = track.contributors.joinToString(separator = ",") { it.name }
            mediaSession?.setMetadata(
                MediaMetadataCompat.Builder().apply {
                    putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
                    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artists)
                    // TODO
                    // putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.durationMs) // Assuming duration is in milliseconds
                    albumArt?.let { putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it) }
                }.build()
            )
        }
    }

    private fun updateMediaSessionPlaybackState(uiState: PlaybackUiState) {
        val playbackState = if (uiState.isPlaying) {
            PlaybackStateCompat.STATE_PLAYING
        } else if (uiState.isLoading) {
            PlaybackStateCompat.STATE_BUFFERING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }

        val actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    playbackState,
                    0L, // Position (update if you track current position)
                    1.0f // Playback speed
                )
                .setActions(actions)
                .build()
        )
    }

    private fun buildNotification(uiState: PlaybackUiState) {
        val sessionToken = mediaSession?.sessionToken
        if (sessionToken == null || uiState.currentTrack == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            return
        }

        val playPauseIcon = if (uiState.isPlaying) {
            R.drawable.ic_pause // Your pause icon
        } else {
            R.drawable.ic_play // Your play icon
        }

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val track = uiState.currentTrack
        val artists = track.contributors.joinToString(separator = ",") { it.name }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_music_note) // Small icon for the status bar
            setContentTitle(uiState.currentTrack.title)
            setContentText(artists)
            // setSubText(uiState.currentTrack.album)
            val largeIconFromSession = mediaSession?.controller?.metadata?.description?.iconBitmap
            largeIconFromSession?.let {
                setLargeIcon(it)
            }
            setContentIntent(pendingContentIntent)
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this@MediaPlaybackService, PlaybackStateCompat.ACTION_STOP
                )
            )
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0, 1, 2) // Indices of actions to show in compact view (Prev, Play/Pause, Next)
            )
            // Add playback actions
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_previous, "Previous",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaPlaybackService, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )
            addAction(
                NotificationCompat.Action(
                    playPauseIcon, "Play/Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaPlaybackService, PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_next, "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaPlaybackService, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )
        }

        // Start the service in the foreground to prevent it from being killed
        startForeground(NOTIFICATION_ID, builder.build())
    }
}

private fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return bitmap
    }
    val bitmap = createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

private const val NOTIFICATION_ICON_SIZE = 96