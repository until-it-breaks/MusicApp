package com.musicapp.data.repositories

import android.net.Uri
import androidx.room.withTransaction
import com.musicapp.data.database.LikedPlaylist
import com.musicapp.data.database.LikedPlaylistDAO
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryDAO
import com.musicapp.data.database.User
import com.musicapp.data.database.UserDAO
import com.musicapp.ui.models.UserModel
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val usersDAO: UserDAO,
    private val likedDAO: LikedPlaylistDAO,
    private val historyDAO: TrackHistoryDAO,
    private val db: MusicAppDatabase
) {
    /**
     * Returns a flow of user given his id.
     */
    fun getUser(userId: String): Flow<User> = usersDAO.getUser(userId)

    /**
     * Updates the profile picture of a given user to the supplied uri.
     */
    suspend fun updateProfilePicture(uri: Uri, userId: String) {
        usersDAO.updateProfilePicture(uri.toString(), userId)
    }

    /**
     * Overrides the profile picture of a given user to null.
     */
    suspend fun removeProfilePicture(userId: String) {
        usersDAO.updateProfilePicture(null, userId)
    }

    /**
     * Updates the username of a given user.
     */
    suspend fun updateUsername(newUsername: String, userId: String) {
        usersDAO.updateUsername(newUsername, userId)
    }

    /**
     * Deletes an user and his data given his id.
     */
    suspend fun deleteUser(userId: String) {
        db.withTransaction {
            removeProfilePicture(userId)
            usersDAO.deleteUser(userId)
            likedDAO.clearLikedTracks(userId)
            historyDAO.clearTrackHistory(userId)
        }
    }

    /**
     * Creates a new user and initializes his liked tracks and track history.
     */
    suspend fun createNewUser(user: UserModel) {
        val now = System.currentTimeMillis()
        val userDb = User(
            userId = user.userId,
            username = user.username,
            email = user.email,
            lastEditTime = now
        )
        var liked = LikedPlaylist(user.userId, now)
        val history = TrackHistory(user.userId, now)

        db.withTransaction {
            usersDAO.insertUser(userDb)
            likedDAO.insertLikedTracksPlaylist(liked)
            historyDAO.insertTrackHistory(history)
        }
    }
}