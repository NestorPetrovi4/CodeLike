package ru.netology.nmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.RepoEntity

@Database(entities = [PostEntity::class, RepoEntity::class], version = 1)
abstract class AppDB :RoomDatabase() {
    abstract fun postDAO() :PostDAO
    companion object {
        @Volatile
        private var instance: AppDB? = null

        fun getInstance(context: Context): AppDB {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDB::class.java, "app.db")
                .allowMainThreadQueries()
                .build()
    }
}