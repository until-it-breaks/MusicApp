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
import kotlinx.coroutines.flow.first

class UserRepository(
    private val usersDAO: UserDAO,
    private val likedDAO: LikedPlaylistDAO,
    private val historyDAO: TrackHistoryDAO,
    private val db: MusicAppDatabase
) {
    fun getUser(userId: String): Flow<User> = usersDAO.getUser(userId)

    suspend fun updateProfilePicture(profilePictureUri: Uri, userId: String) {
        val user = usersDAO.getUser(userId)
        val currentProfilePicture = user.first().profilePictureUri // TODO maybe delete the current picture saved in memory
        usersDAO.updateProfilePicture(profilePictureUri.toString(), userId)
    }

    suspend fun updateUsername(newUsername: String, userId: String) {
        usersDAO.updateUsername(newUsername, userId)
    }

    suspend fun deleteUser(user: User) {
        usersDAO.deleteUser(user)
        // TODO Delete firebase auth entry and maybe delete other related stuff too.
    }

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