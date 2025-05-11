package com.musicapp.data.repositories

import android.content.ContentResolver
import com.musicapp.data.database.User
import com.musicapp.data.database.UsersDAO

class UsersRepository(
    private val dao: UsersDAO,
    private val contentResolver: ContentResolver
) {
    suspend fun getUsers(): List<User> = dao.getUsers()

    suspend fun upsertUser(user: User) = dao.upsertUser(user)

    suspend fun deleteUser(user: User) = dao.deleteUser(user)
}