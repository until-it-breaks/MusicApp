package com.musicapp.data.repositories

import com.musicapp.data.database.User
import com.musicapp.data.database.UsersDAO
import com.musicapp.ui.models.UserModel
import com.musicapp.ui.models.toDbEntity

class UsersRepository(
    private val dao: UsersDAO
) {
    suspend fun getUsers(): List<User> = dao.getUsers()

    suspend fun upsertUser(user: UserModel) {
        dao.upsertUser(user.toDbEntity())
    }

    suspend fun deleteUser(user: User) = dao.deleteUser(user)
}