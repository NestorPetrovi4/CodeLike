package ru.netology.nmedia.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.dao.PostDAOImpl

class AppDB private constructor(db: SQLiteDatabase) {
    val postDAO: PostDAO = PostDAOImpl(db)

    companion object {
        @Volatile
        private var instance: AppDB? = null

        fun getInstance(context: Context): AppDB {
            return instance ?: synchronized(this) {
                instance ?: AppDB(
                    buildDatabase(context, arrayOf(PostDAOImpl.DDL, PostDAOImpl.DDLRepo))
                ).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context, DDLs: Array<String>) =
            DbHelper(context, 1, "app.db", DDLs).writableDatabase
    }
}

class DbHelper(context: Context, dbVersion: Int, dbName: String, private val DDLs: Array<String>) :
    SQLiteOpenHelper(context, dbName, null, dbVersion) {
    override fun onCreate(db: SQLiteDatabase) {
        DDLs.forEach {
            db.execSQL(it)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        TODO("Not implemented")
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        TODO("Not implemented")
    }
}