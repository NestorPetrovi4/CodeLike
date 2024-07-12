package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.RepoEntity

@Dao
interface PostDAO {

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE CASE WHEN :readMe = TRUE THEN readMe = 1 ELSE readMe = 0 END ORDER BY id DESC")
    fun getReadMeAll(readMe: Boolean = true): Flow<List<PostEntity>>

    @Query("""SELECT * FROM PostEntity WHERE id = :id;""")
    suspend fun getById(id: Int): PostEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(posts: List<PostEntity>)

    @Query(
        """UPDATE PostEntity SET likes = likes +  1, likedByMe = 1, sendServer = 1 WHERE id = :id;""")
    suspend fun likeById(id: Int)

    @Query(
        """UPDATE PostEntity SET likes = likes  -1, likedByMe = 0, sendServer = 1 WHERE id = :id;""")
    suspend fun unLikeById(id: Int)

    @Query("""UPDATE PostEntity SET shared = shared + 1 WHERE id = :id;""")
    suspend fun sharedById(id: Int)

    @Query("""DELETE FROM PostEntity WHERE id = :id""")
    suspend fun removeById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRepoValue(repo: RepoEntity)

    @Query("""DELETE FROM RepoEntity WHERE id = :key""")
    suspend fun removeRepoKey(key: String)

    @Query("SELECT value FROM RepoEntity WHERE id = :key")
    suspend fun getRepoKey(key: String): String

    @Query(
        """UPDATE PostEntity SET sendServer = 0 WHERE id = :id;""")
    suspend fun sendenServerById(id: Int)

    @Query("SELECT * FROM PostEntity ORDER BY id DESC LIMIT 1")
    suspend fun getMaxPostId(): List<PostEntity>

    @Query("SELECT * FROM PostEntity WHERE readMe = 0")
    suspend fun getNotReadMeMaxPost(): List<PostEntity>

    @Query("UPDATE PostEntity SET readMe = 1 WHERE readMe = 0")
    suspend fun setReadAll()
    @Query("DELETE FROM PostEntity")
    suspend fun clearAll()
}