package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.RepoEntity

@Database(entities = [PostEntity::class, RepoEntity::class], version = 1)
abstract class AppDB :RoomDatabase() {
    abstract fun postDAO() :PostDAO
}