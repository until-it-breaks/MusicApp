package com.musicapp.data.repositories

import android.content.ContentResolver
import com.musicapp.data.database.User
import com.musicapp.data.database.UsersDAO
import kotlinx.coroutines.flow.Flow

class UsersRepository(
    private val dao: UsersDAO,
    private val contentResolver: ContentResolver
) {
    val users: Flow<List<User>> = dao.getUsers()

    suspend fun upsertUser(user: User) = dao.insertUser(user)

    suspend fun deleteTrack(user: User) = dao.deleteUser(user)
}