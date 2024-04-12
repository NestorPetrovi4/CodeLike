package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.RepoEntity

@Dao
interface PostDAO {
//    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
//    fun getAll(): LiveData<List<PostEntity>>
//
//    @Insert
//    fun insert(post: PostEntity)
//
//
//    @Query("""UPDATE PostEntity SET content = :content WHERE id = :id""")
//    fun updateContentById(id: Int, content: String)
//
//    @Query(
//        """UPDATE PostEntity SET likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
//                        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END WHERE id = :id;"""
//    )
//    fun likeById(id: Int)
//
//    @Query("""UPDATE PostEntity SET shared = shared + 1 WHERE id = :id;""")
//    fun sharedById(id: Int)
//
//    @Query("""DELETE FROM PostEntity WHERE id = :id""")
//    fun removeById(id: Int)
//    fun save(post: PostEntity) =
//        if (post.id == 0) insert(post) else updateContentById(post.id, post.content)
//
    fun addRepoValue(key: String, value: String) {
        getRepoKey(key)?.let {
            updateRepo(key, value)
        } ?: insertRepo(RepoEntity(key, value))

    }

    @Insert
    fun insertRepo(repo: RepoEntity)

    @Query("""UPDATE RepoEntity SET value = :value WHERE id = :key""")
    fun updateRepo(key: String, value: String)

    @Query("""DELETE FROM RepoEntity WHERE id = :key""")
    fun removeRepoKey(key: String)

    @Query("SELECT value FROM RepoEntity WHERE id = :key")
    fun getRepoKey(key: String): String
}