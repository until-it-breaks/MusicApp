package com.musicapp.data.repositories

import androidx.room.withTransaction
import com.musicapp.data.database.LikedPlaylist
import com.musicapp.data.database.LikedPlaylistDAO
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryDAO
import com.musicapp.data.database.User
import com.musicapp.data.database.UserDAO
import com.musicapp.ui.models.UserModel

class UserRepository(
    private val usersDAO: UserDAO,
    private val likedDAO: LikedPlaylistDAO,
    private val historyDAO: TrackHistoryDAO,
    private val db: MusicAppDatabase
) {
    suspend fun getUsers(): List<User> = usersDAO.getUsers() // Could be useful. Maybe not.

    suspend fun deleteUser(user: User) = usersDAO.deleteUser(user) // TODO Can be improved

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