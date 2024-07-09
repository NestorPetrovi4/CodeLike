package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>>
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
    suspend fun saveWithAttachment(post: Post, file: File)
    suspend fun signIn(login: String, password: String)
    suspend fun signUp(name: String, login: String, password: String)
    suspend fun signUp(name: String, login: String, password: String, file: File)
}