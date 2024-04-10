package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun getById(id: Int): Post
    fun likeById(id: Int): Post
    fun unlikeById(id: Int): Post
    fun sharedById(id: Int)
    fun removeByID(id: Int)
    fun save(post: Post)
    fun addRepoValue(key: String, value: String)
    fun removeRepoKey(key: String)
    fun getRepoKey(key: String): String
}