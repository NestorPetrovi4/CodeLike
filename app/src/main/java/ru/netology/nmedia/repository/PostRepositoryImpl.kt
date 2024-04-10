package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.dto.Post
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

class PostRepositoryImpl(private val dao: PostDAO) : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeTokenList = object : TypeToken<List<Post>>() {}
    private val typeTokenPost = object : TypeToken<Post>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): List<Post> {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) error("Response code: ${response.code}")
        return response.let { it.body?.string() ?: throw RuntimeException("Body is null") }
            .let { gson.fromJson(it, typeTokenList.type) }
    }

    override fun getById(id: Int): Post {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) error("Response code: ${response.code}")
        return response.let { it.body?.string() ?: throw RuntimeException("Body is null") }
            .let { gson.fromJson(it, typeTokenPost.type) }
    }

    override fun likeById(id: Int):Post {
        val request: Request = Request.Builder()
            .post("".toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) error("Response code: ${response.code}")
        return response.let { it.body?.string() ?: throw RuntimeException("Body is null") }
            .let { gson.fromJson(it, typeTokenPost.type) }
    }

    override fun unlikeById(id: Int): Post {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) error("Response code: ${response.code}")
        return response.let { it.body?.string() ?: throw RuntimeException("Body is null") }
            .let { gson.fromJson(it, typeTokenPost.type) }
    }

    override fun sharedById(id: Int) {
        //
    }

    override fun removeByID(id: Int) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun addRepoValue(key: String, value: String) {
        dao.addRepoValue(key, value)
    }

    override fun removeRepoKey(key: String) {
        dao.removeRepoKey(key)
    }

    override fun getRepoKey(key: String) = dao.getRepoKey(key)
}