package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {
    fun getAll(callback: Callback<List<Post>>)
    fun getById(id: Int, callback: Callback<Post>)
    fun likeById(id: Int, callback: Callback<Post>)
    fun unlikeById(id: Int, callback: Callback<Post>)
    fun sharedById(id: Int)
    fun removeByID(id: Int, callback: Callback<Unit>)
    fun save(post: Post, callback: Callback<Post>)
    fun getBASE_URL():String
    fun addRepoValue(key: String, value: String)
    fun removeRepoKey(key: String)
    fun getRepoKey(key: String): String

    interface Callback<T>{
        fun onSuccess(result: T)
        fun onError(exception: Exception)
    }
}