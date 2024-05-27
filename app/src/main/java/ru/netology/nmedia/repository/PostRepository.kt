package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun getById(id: Int): Post
    suspend fun likeById(id: Int)
    suspend fun unLikeById(id: Int)
    suspend fun sharedById(id: Int)
    suspend fun removeByID(id: Int)
    suspend fun save(post: Post)
    fun getBASE_URL():String
    suspend fun addRepoValue(key: String, value: String)
    suspend fun removeRepoKey(key: String)
    suspend fun getRepoKey(key: String): String
    fun getNewerCount(): Flow<Int>
    suspend fun setReadAll()
}