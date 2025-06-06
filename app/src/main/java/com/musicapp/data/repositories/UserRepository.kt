package com.musicapp.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.room.withTransaction
import com.musicapp.data.database.LikedPlaylist
import com.musicapp.data.database.LikedPlaylistDAO
import com.musicapp.data.database.MusicAppDatabase
import com.musicapp.data.database.TrackHistory
import com.musicapp.data.database.TrackHistoryDAO
import com.musicapp.data.database.User
import com.musicapp.data.database.UserDAO
import com.musicapp.ui.models.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class UserRepository(
    private val usersDAO: UserDAO,
    private val likedDAO: LikedPlaylistDAO,
    private val historyDAO: TrackHistoryDAO,
    private val db: MusicAppDatabase,
    private val context: Context
) {
    fun getUser(userId: String): Flow<User> = usersDAO.getUser(userId)

    suspend fun saveProfilePictureToInternalStorage(uri: Uri, userId: String): Uri? {
        return withContext(Dispatchers.IO) {
            val fileName = "profile_picture_$userId.jpg"
            val file = File(context.filesDir, fileName)

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("UserRepository", "Successfully saved profile picture to: ${file.absolutePath}")
                return@withContext file.toUri()
            } catch (e: Exception) {
                Log.e("UserRepository", "Error saving profile picture to internal storage", e)
                return@withContext null
            }
        }
    }

    suspend fun updateProfilePicture(uri: Uri, userId: String) {
        val newInternalUri = saveProfilePictureToInternalStorage(uri, userId)
        if (newInternalUri != null) {
            usersDAO.updateProfilePicture(newInternalUri.toString(), userId)
            Log.d("UserRepository", "Database updated with new profile picture URI: $newInternalUri")
        } else {
            usersDAO.updateProfilePicture(Uri.EMPTY.toString(), userId)
            Log.e("UserRepository", "Failed to save profile picture to internal storage for $userId")
        }
    }

    suspend fun removeProfilePicture(userId: String) {
        withContext(Dispatchers.IO) {
            val fileName = "profile_picture_$userId.jpg"
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.delete()
                Log.d("UserRepository", "Deleted profile picture file: ${file.absolutePath}")
            }
            usersDAO.updateProfilePicture(Uri.EMPTY.toString(), userId)
            Log.d("UserRepository", "Profile picture URI set to EMPTY in DB for userId: $userId")
        }
    }

    suspend fun updateUsername(newUsername: String, userId: String) {
        usersDAO.updateUsername(newUsername, userId)
    }

    suspend fun deleteUser(user: User) {
        usersDAO.deleteUser(user)
        // TODO Delete firebase auth entry and maybe delete other related stuff too.
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