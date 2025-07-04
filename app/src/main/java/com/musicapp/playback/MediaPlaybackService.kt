package com.musicapp.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.musicapp.MainActivity
import com.musicapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.abs

private const val TAG = "MediaPlaybackService"

@UnstableApi
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
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

        createNotificationChannel()

        initializeExoPlayer()
        initializeMediaSession()
        initializeNotificationManager()

        observePlaybackState()
    }

    private fun initializeExoPlayer() {
        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(DefaultTrackSelector(this))
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d(TAG, "ExoPlayer isPlaying changed: $isPlaying")
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d(TAG, "ExoPlayer playback state changed: $playbackState")
                        if (playbackState == Player.STATE_ENDED) {
                            Log.d(TAG, "ExoPlayer: Track ended naturally. Triggering next.")
                            mediaPlayerManager.playNext()
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        Log.d(TAG, "ExoPlayer: Media item transition to ${mediaItem?.mediaMetadata?.title}")
                    }
                })
            }
        mediaPlayerManager.setExoPlayerInstance(exoPlayer)
    }

    private fun initializeMediaSession() {
        exoPlayer?.let { player ->
            mediaSession = MediaSession.Builder(this, player)
                .setSessionActivity(getMediaSessionActivityPendingIntent())
                .build()
            Log.d(TAG, "MediaSession created and linked to ExoPlayer")
        } ?: run {
            Log.e(TAG, "Failed to initialize MediaSession: ExoPlayer is null")
        }
    }

    private fun initializeNotificationManager() {
        exoPlayer?.let { player ->
            mediaSession?.let { session ->
                playerNotificationManager = PlayerNotificationManager.Builder(
                    this,
                    NOTIFICATION_ID,
                    CHANNEL_ID
                )
                    .setChannelNameResourceId(R.string.channel_name)
                    .setChannelDescriptionResourceId(R.string.channel_description)
                    .setSmallIconResourceId(R.drawable.ic_music_note)
                    .setMediaDescriptionAdapter(
                        PlaybackDescriptionAdapter(
                            context = this,
                            mediaPlayerManager = mediaPlayerManager,
                            serviceScope = serviceScope,
                            notificationActivityPendingIntent = getMediaSessionActivityPendingIntent()
                        )
                    )
                    .setNotificationListener(PlaybackNotificationListener(this, mediaPlayerManager))
                    .build()
                    .apply {
                        setPlayer(player)
                        setMediaSessionToken(session.platformToken)
                        setUseStopAction(true)
                        setUsePlayPauseActions(true)
                        setUseNextAction(true)
                        setUsePreviousAction(true)
                    }
                Log.d(TAG, "PlayerNotificationManager initialized")
            }
        } ?: run {
            Log.e(TAG, "Failed to initialize PlayerNotificationManager: ExoPlayer or MediaSession is null")
        }
    }

    private fun observePlaybackState() {
        serviceScope.launch {
            mediaPlayerManager.playbackState.collectLatest { uiState ->
                Log.d(TAG, "MediaPlayerManager PlaybackUiState updated: $uiState")

                val currentQueueItem = uiState.currentQueueItem
                val exoPlayerCurrentMediaItem = exoPlayer?.currentMediaItem

                if (currentQueueItem != null) {
                    val mediaItem = createMediaItem(currentQueueItem)

                    if (shouldUpdateMediaItem(exoPlayerCurrentMediaItem, mediaItem)) {
                        Log.d(TAG, "Setting new MediaItem for ExoPlayer: ${currentQueueItem.track.title}")
                        exoPlayer?.setMediaItem(mediaItem)
                        exoPlayer?.prepare()
                        if (uiState.isPlaying) {
                            exoPlayer?.play()
                        } else {
                            exoPlayer?.pause()
                        }
                    } else {
                        syncPlaybackState(uiState)
                    }
                    syncSeekPosition(uiState.currentPositionMs)
                } else {
                    handleNoCurrentTrack()
                }
            }
        }
    }

    private fun shouldUpdateMediaItem(currentItem: MediaItem?, newItem: MediaItem): Boolean {
        return currentItem == null ||
                newItem.mediaId != currentItem.mediaId ||
                newItem.localConfiguration?.uri != currentItem.localConfiguration?.uri
    }

    private fun syncPlaybackState(uiState: PlaybackUiState) {
        if (uiState.isPlaying && exoPlayer?.isPlaying == false) {
            exoPlayer?.play()
            Log.d(TAG, "ExoPlayer play triggered by MediaPlayerManager")
        } else if (!uiState.isPlaying && exoPlayer?.isPlaying == true) {
            exoPlayer?.pause()
            Log.d(TAG, "ExoPlayer pause triggered by MediaPlayerManager")
        }
    }

    private fun syncSeekPosition(positionMs: Long) {
        if (abs((exoPlayer?.currentPosition ?: 0L) - positionMs) > 1000L) {
            exoPlayer?.seekTo(positionMs)
        }
    }

    private fun handleNoCurrentTrack() {
        if (exoPlayer?.isPlaying == true || exoPlayer?.isLoading == true) {
            Log.d(TAG, "Stopping ExoPlayer due to null currentTrack in MediaPlayerManager")
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d(TAG, "onGetSession called.")
        return mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MediaPlaybackService destroyed.")
        cleanupResources()
        mediaPlayerManager.release()
    }

    private fun cleanupResources() {
        serviceJob.cancel()
        playerNotificationManager?.setPlayer(null)
        exoPlayer?.release()
        exoPlayer = null
        mediaSession?.release()
        mediaSession = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_description)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun createMediaItem(queueItem: QueueItem): MediaItem {
        return MediaItem.Builder()
            .setMediaId(queueItem.id.toString())
            .setUri(queueItem.track.previewUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(queueItem.track.title)
                    .setArtist(queueItem.track.contributors.joinToString { it.name })
                    .setArtworkUri(queueItem.track.bigPictureUri)
                    .build()
            )
            .build()
    }

    private fun getMediaSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}