package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.dto.Post
import java.io.IOException
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

    override fun getAll(callback: PostRepository.Callback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()
        return client.newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callback.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            if (!response.isSuccessful) throw RuntimeException("Response code: ${response.code}")
                            callback.onSuccess(response.let {
                                it.body?.string() ?: throw RuntimeException("Body is null")
                            }
                                .let { gson.fromJson(it, typeTokenList.type) })
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }
                }
            )
    }

    override fun getById(id: Int, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()
        return client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) throw RuntimeException("Response code: ${response.code}")
                        callback.onSuccess(response.let {
                            it.body?.string() ?: throw RuntimeException("Body is null")
                        }
                            .let { gson.fromJson(it, typeTokenPost.type) })
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            }
        )
    }

    override fun likeById(id: Int, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder()
            .post("".toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()
        return client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) throw RuntimeException("Response code: ${response.code}")
                        callback.onSuccess(response.let {
                            it.body?.string() ?: throw RuntimeException("Body is null")
                        }
                            .let { gson.fromJson(it, typeTokenPost.type) })
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })
    }

    override fun unlikeById(id: Int, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()

        return client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) throw RuntimeException("Response code: ${response.code}")
                        callback.onSuccess(response.let {
                            it.body?.string() ?: throw RuntimeException("Body is null")
                        }
                            .let { gson.fromJson(it, typeTokenPost.type) })
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })
    }

    override fun sharedById(id: Int) {
        //
    }

    override fun removeByID(id: Int, callback: PostRepository.Callback<Unit>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) throw RuntimeException("Response code: ${response.code}")
                        callback.onSuccess(Unit)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })
    }

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) throw RuntimeException("Response code: ${response.code}")
                        callback.onSuccess(post)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })
    }

    override fun getBASE_URL(): String {
        return BASE_URL
    }

    override fun addRepoValue(key: String, value: String) {
        dao.addRepoValue(key, value)
    }

    override fun removeRepoKey(key: String) {
        dao.removeRepoKey(key)
    }

    override fun getRepoKey(key: String) = dao.getRepoKey(key)
}