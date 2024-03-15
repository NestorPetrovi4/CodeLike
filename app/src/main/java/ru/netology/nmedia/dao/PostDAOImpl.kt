package ru.netology.nmedia.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import ru.netology.nmedia.dto.Post

class PostDAOImpl(private val db: SQLiteDatabase) : PostDAO {
    companion object {
        val DDL = """
        CREATE TABLE ${PostColumns.TABLE} (
            ${PostColumns.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${PostColumns.COLUMN_AUTHOR} TEXT NOT NULL,
            ${PostColumns.COLUMN_PUBLISHED} TEXT NOT NULL,
            ${PostColumns.COLUMN_CONTENT} TEXT NOT NULL,
            ${PostColumns.COLUMN_LIKES} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_LIKEDBYME} BOOLEAN NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_SHARED} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_VIEWOPEN} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_VIDEOURL} TEXT NOT NULL DEFAULT "" 
        );
        """.trimIndent()
        val DDLRepo = """
        CREATE TABLE ${RepoColumns.TABLEREPO}(
            ${PostColumns.COLUMN_ID} TEXT PRIMARY KEY,
            ${RepoColumns.COLUMN_VALUE} TEXT NOT NULL DEFAULT ""
        );
        """.trimIndent()
    }

    override fun getAll(): List<Post> {
        val posts = mutableListOf<Post>()
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "${PostColumns.COLUMN_ID} DESC"
        ).use {
            while (it.moveToNext()) {
                posts.add(map(it))
            }
        }
        return posts
    }

    override fun likeById(id: Int) {
        db.execSQL(
            """UPDATE posts SET
            likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
            likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END 
            WHERE id = ?;
        """.trimIndent(), arrayOf(id)
        )
    }

    override fun sharedById(id: Int) {
        db.execSQL(
            """UPDATE posts SET
            shared = shared + 1
            WHERE id = ?;
        """.trimIndent(), arrayOf(id.toString())
        )
    }

    override fun removeById(id: Int) {
        db.delete(PostColumns.TABLE, "${PostColumns.COLUMN_ID} =?", arrayOf(id.toString()))
    }

    override fun save(post: Post): Post {
        val values = ContentValues().apply {
            if (post.id != 0) {
                put(PostColumns.COLUMN_ID, post.id)
            }
            put(PostColumns.COLUMN_AUTHOR, "Netology")
            put(PostColumns.COLUMN_PUBLISHED, "Now")
            put(PostColumns.COLUMN_CONTENT, post.content)
        }
        //val id = db.replace(PostColumns.TABLE, null, values)
        val id = if (post.id != 0) {
            db.update(
                PostColumns.TABLE,
                values,
                "${PostColumns.COLUMN_ID} = ?",
                arrayOf(post.id.toString())
            )
            post.id
        } else {
            db.insert(PostColumns.TABLE, null, values)
        }
        db.query(
            PostColumns.TABLE, PostColumns.ALL_COLUMNS, "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString()), null, null, null
        ).use {
            it.moveToNext()
            return map(it)
        }
    }

    override fun addRepoValue(key: String, value: String) {
        val values = ContentValues().apply {
            put(PostColumns.COLUMN_ID, key)
            put(RepoColumns.COLUMN_VALUE, value)
        }
        db.replace(RepoColumns.TABLEREPO, null, values)
    }

    override fun removeRepoKey(key: String) {
        db.delete(RepoColumns.TABLEREPO, "${PostColumns.COLUMN_ID} =?", arrayOf(key))
    }

    override fun getRepoKey(key: String): String {
        db.query(
            RepoColumns.TABLEREPO,
            arrayOf(RepoColumns.COLUMN_VALUE),
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(key),
            null,
            null,
            null
        ).use {
            return if (it.moveToNext()) it.getString(it.getColumnIndexOrThrow(RepoColumns.COLUMN_VALUE))
            else ""
        }
    }

    private fun map(cursor: Cursor): Post {
        with(cursor) {
            return Post(
                id = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_ID)),
                author = getString(getColumnIndexOrThrow(PostColumns.COLUMN_AUTHOR)),
                published = getString(getColumnIndexOrThrow(PostColumns.COLUMN_PUBLISHED)),
                content = getString(getColumnIndexOrThrow(PostColumns.COLUMN_CONTENT)),
                likes = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKES)),
                likedByMe = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKEDBYME)) != 0,
                shared = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_SHARED)),
                viewOpen = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_VIEWOPEN)),
                videoURL = getString(getColumnIndexOrThrow(PostColumns.COLUMN_VIDEOURL))
            )
        }
    }

}

object PostColumns {
    const val TABLE = "posts"
    const val COLUMN_ID = "id"
    const val COLUMN_AUTHOR = "author"
    const val COLUMN_PUBLISHED = "published"
    const val COLUMN_CONTENT = "content"
    const val COLUMN_LIKES = "likes"
    const val COLUMN_LIKEDBYME = "likedByMe"
    const val COLUMN_SHARED = "shared"
    const val COLUMN_VIEWOPEN = "viewOpen"
    const val COLUMN_VIDEOURL = "videoURL"
    val ALL_COLUMNS = arrayOf(
        COLUMN_ID,
        COLUMN_AUTHOR,
        COLUMN_PUBLISHED,
        COLUMN_CONTENT,
        COLUMN_LIKES,
        COLUMN_LIKEDBYME,
        COLUMN_SHARED,
        COLUMN_VIEWOPEN,
        COLUMN_VIDEOURL
    )

}

object RepoColumns {
    const val TABLEREPO = "repository"
    const val COLUMN_VALUE = "value"
    val ALL_COLUMNS = arrayOf(
        PostColumns.COLUMN_ID,
        COLUMN_VALUE
    )

}