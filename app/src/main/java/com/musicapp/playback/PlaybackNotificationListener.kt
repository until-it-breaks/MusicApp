package com.musicapp.playback

import android.app.Notification
import android.app.Service
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager

private const val TAG = "PlaybackNotificationListener"

@UnstableApi
class PlaybackNotificationListener(
    private val service: MediaPlaybackService,
    private val mediaPlayerManager: MediaPlayerManager
) : PlayerNotificationManager.NotificationListener {

    override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
        if (ongoing) {
            service.startForeground(notificationId, notification)
            Log.d(TAG, "Notification posted. Service is now foreground.")
        } else {
            service.stopForeground(Service.STOP_FOREGROUND_DETACH)
            Log.d(TAG, "Notification not ongoing. Service detached from foreground.")
        }
    }

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        Log.d(TAG, "Notification cancelled. Dismissed by user: $dismissedByUser")
        if (dismissedByUser) {
            mediaPlayerManager.stop()
        }
        service.stopSelf()
    }
}