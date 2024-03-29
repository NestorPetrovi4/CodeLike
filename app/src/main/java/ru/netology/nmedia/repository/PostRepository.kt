package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun likeById(id: Int)
    fun sharedById(id: Int)
    fun removeByID(id: Int)
    fun save(post: Post)
    fun addRepoValue(key: String, value: String)
    fun removeRepoKey(key: String)
    fun getRepoKey(key: String): String
}