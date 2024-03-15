package ru.netology.nmedia.dao

import ru.netology.nmedia.dto.Post

interface PostDAO {
    fun getAll(): List<Post>
    fun likeById(id: Int)
    fun sharedById(id: Int)
    fun removeById(id: Int)
    fun save(post: Post): Post
    fun addRepoValue(key: String, value: String)
    fun removeRepoKey(key: String)
    fun getRepoKey(key: String): String
}