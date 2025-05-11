package com.musicapp.data.repositories

import android.content.ContentResolver
import com.musicapp.data.database.Playlist
import com.musicapp.data.database.PlaylistsDAO
import com.musicapp.data.database.LikedTracksDAO
import com.musicapp.data.database.LikedTracksPlaylist
import com.musicapp.data.database.LikedTracksPlaylistWithTracks
import com.musicapp.data.database.PlaylistWithTracks
import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryDAO
import com.musicapp.data.database.TrackHistoryWithTracks

class PlaylistsRepository(
    private val playlistDAO: PlaylistsDAO,
    private val likedTracksDAO: LikedTracksDAO,
    private val trackHistoryDAO: TrackHistoryDAO,

    private val contentResolver: ContentResolver
) {
    // Normal playlists

    suspend fun getUserPlaylists(userId: String): List<Playlist> = playlistDAO.getUserPlaylists(userId)

    suspend fun getUserPlaylistsWithTracks(playlistId: String): PlaylistWithTracks = playlistDAO.getPlaylistWithTracks(playlistId)

    suspend fun upsertPlaylist(playlist: Playlist) = playlistDAO.upsertPlaylist(playlist)

    suspend fun deletePlaylist(playlist: Playlist) = playlistDAO.deletePlaylist(playlist)

    // Liked tracks playlist

    suspend fun getLikedTracks(userId: String): LikedTracksPlaylist = likedTracksDAO.getLikedTracksPlaylist(userId)

    suspend fun getLikedTracksWithTracks(userId: String): LikedTracksPlaylistWithTracks = likedTracksDAO.getLikedTracksPlaylistWithTracks(userId)

    suspend fun upsertLikedTracks(likedTracksPlaylist: LikedTracksPlaylist) = likedTracksDAO.upsertLikedTracksPlaylist(likedTracksPlaylist)

    suspend fun deletedLikedTracks(likedTracksPlaylist: LikedTracksPlaylist) = likedTracksDAO.deleteLikedTracksPlaylist(likedTracksPlaylist)

    // History tracks

    suspend fun getTrackHistory(userId: String): TrackHistory = trackHistoryDAO.getTrackHistory(userId)

    suspend fun getTrackHistoryWithTracks(userId: String): TrackHistoryWithTracks = trackHistoryDAO.getTrackHistoryWithTracks(userId)

    suspend fun upsertTrackHistory(trackHistory: TrackHistory) = trackHistoryDAO.upsertTrackHistory(trackHistory)

    suspend fun deleteTrackHistory(trackHistory: TrackHistory) = trackHistoryDAO.deleteTrackHistory(trackHistory)
}