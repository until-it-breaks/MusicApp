package com.musicapp.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service // Important for Service.STOP_FOREGROUND_* flags
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import com.musicapp.R
import com.musicapp.MainActivity
import com.musicapp.ui.models.TrackModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.common.util.UnstableApi // Ensure this import is here for @UnstableApi
import kotlin.math.abs

@UnstableApi // Opt-in annotation for unstable Media3 APIs
class MediaPlaybackService : MediaSessionService() {

    private val mediaPlayerManager: MediaPlayerManager by inject()
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var playerNotificationManager: PlayerNotificationManager? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "music_playback_channel"
        // CHANNEL_NAME is defined in strings.xml now
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MediaPlaybackService", "Service onCreate")

        createNotificationChannel() // Ensure channel is created before notification manager

        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(DefaultTrackSelector(this)) // Optional: customize track selection
            .build()
            .apply {
                // Set callbacks for player state changes (optional, but good for logging/debugging)
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d("MediaPlaybackService", "ExoPlayer isPlaying changed: $isPlaying")
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d("MediaPlaybackService", "ExoPlayer playback state changed: $playbackState")
                        // Check if playback ended naturally, then trigger next
                        if (playbackState == Player.STATE_ENDED) {
                            Log.d("MediaPlaybackService", "ExoPlayer: Track ended naturally. Triggering next.")
                            mediaPlayerManager.playNext() // Let MediaPlayerManager handle queue logic
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        Log.d("MediaPlaybackService", "ExoPlayer: Media item transition to ${mediaItem?.mediaMetadata?.title}")
                    }
                })
            }

        // Initialize MediaSession with the ExoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer!!) // Use non-null asserted player
            .setSessionActivity(getMediaSessionActivityPendingIntent()) // Set the activity to open when notification is clicked
            .build()
        Log.d("MediaPlaybackService", "MediaSession created and linked to ExoPlayer")

        // Initialize PlayerNotificationManager
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.channel_name) // string resource for channel name
            .setChannelDescriptionResourceId(R.string.channel_description) // string resource for channel description
            .setSmallIconResourceId(R.drawable.ic_music_note) // Your small notification icon
            // Use external classes for adapter and listener
            .setMediaDescriptionAdapter(
                PlaybackDescriptionAdapter(
                    this, // Context
                    mediaPlayerManager,
                    serviceScope,
                    getMediaSessionActivityPendingIntent() // PendingIntent for content click
                )
            )
            .setNotificationListener(PlaybackNotificationListener(this, mediaPlayerManager)) // Listener for notification events
            .build()
            .apply {
                setPlayer(exoPlayer) // Link the notification manager to the player
                // Correctly cast to the framework's MediaSession.Token
                setMediaSessionToken(mediaSession!!.token)
                setUseStopAction(true) // Show the X button to stop the service
                setUsePlayPauseActions(true)
                setUseNextAction(true)
                setUsePreviousAction(true)
            }


        // Observe playback state from MediaPlayerManager and update ExoPlayer
        serviceScope.launch {
            mediaPlayerManager.playbackState.collectLatest { uiState ->
                Log.d("MediaPlaybackService", "MediaPlayerManager PlaybackUiState updated: $uiState")

                val currentTrack = uiState.currentTrack
                val exoPlayerCurrentMediaItem = exoPlayer?.currentMediaItem

                if (currentTrack != null) {
                    val mediaItem = createMediaItem(currentTrack)

                    // Check if the current ExoPlayer media item is different from the MediaPlayerManager's current track
                    if (exoPlayerCurrentMediaItem == null || mediaItem.mediaId != exoPlayerCurrentMediaItem.mediaId ||
                        mediaItem.playbackProperties?.uri != exoPlayerCurrentMediaItem.playbackProperties?.uri
                    ) {
                        Log.d("MediaPlaybackService", "Setting new MediaItem for ExoPlayer: ${currentTrack.title}")
                        exoPlayer?.setMediaItem(mediaItem)
                        exoPlayer?.prepare()
                        if (uiState.isPlaying) { // Only play if manager wants to play
                            exoPlayer?.play()
                        } else {
                            exoPlayer?.pause()
                        }
                    } else if (uiState.isPlaying && exoPlayer?.isPlaying == false) {
                        exoPlayer?.play()
                        Log.d("MediaPlaybackService", "ExoPlayer play triggered by MediaPlayerManager")
                    } else if (!uiState.isPlaying && exoPlayer?.isPlaying == true) {
                        exoPlayer?.pause()
                        Log.d("MediaPlaybackService", "ExoPlayer pause triggered by MediaPlayerManager")
                    }

                    // Update seek position if significant difference (optional, avoid frequent seeking)
                    if (abs(exoPlayer?.currentPosition ?: (0L - uiState.currentPositionMs)) > 1000L) {
                        exoPlayer?.seekTo(uiState.currentPositionMs)
                    }

                } else {
                    // No track in MediaPlayerManager, stop ExoPlayer and notification
                    if (exoPlayer?.isPlaying == true || exoPlayer?.isLoading == true) {
                        Log.d("MediaPlaybackService", "Stopping ExoPlayer due to null currentTrack in MediaPlayerManager")
                        exoPlayer?.stop()
                        exoPlayer?.clearMediaItems() // Clear queue
                    }
                    stopForeground(Service.STOP_FOREGROUND_REMOVE) // Remove notification and foreground status
                    // The playerNotificationManager?.setPlayer(null) in onDestroy handles releasing notification
                    // We don't release mediaSession here unless the service is truly stopping,
                    // as onGetSession might be called again if it's merely a transient state.
                }
            }
        }
    }

    // This method is called by MediaSessionService when it needs a MediaSession
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d("MediaPlaybackService", "onGetSession called.")
        return mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MediaPlaybackService", "MediaPlaybackService destroyed.")
        serviceJob.cancel() // Cancel all coroutines
        playerNotificationManager?.setPlayer(null) // Detach player from notification
        exoPlayer?.release() // Release ExoPlayer resources
        exoPlayer = null
        mediaSession?.release() // Release MediaSession resources
        mediaSession = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_name), // Use resource string here
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_description) // Use resource string here
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    // Helper to create MediaItem from TrackModel
    private fun createMediaItem(track: TrackModel): MediaItem {
        return MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.previewUri) // Use the previewUri for playback
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.contributors.joinToString { it.name })
                    .setArtworkUri(track.bigPictureUri) // Use largePictureUri for artwork
                    .build()
            )
            .build()
    }

    private fun getMediaSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}