package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.dto.Post
import java.lang.RuntimeException

class PostRepositoryImpl(private val dao: PostDAO) : PostRepository {
    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
    }

    override fun getAll(callback: PostRepository.Callback<List<Post>>) {
        ApiService.service.getAll()
            .enqueue(
                object : Callback<List<Post>> {
                    override fun onResponse(
                        call: retrofit2.Call<List<Post>>,
                        response: retrofit2.Response<List<Post>>
                    ) {
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException("Response code: ${response.code()}"))
                            return
                        }
                        callback.onSuccess(
                            response.body() ?: throw RuntimeException("Body is null")
                        )
                    }

                    override fun onFailure(call: retrofit2.Call<List<Post>>, e: Throwable) {
                        callback.onError(Exception(e))
                    }
                }
            )
    }

    override fun getById(id: Int, callback: PostRepository.Callback<Post>) {
        ApiService.service.getById(id)
            .enqueue(
                object : Callback<Post> {
                    override fun onFailure(p0: retrofit2.Call<Post>, e: Throwable) {
                        callback.onError(Exception(e))
                    }

                    override fun onResponse(
                        call: retrofit2.Call<Post>,
                        response: retrofit2.Response<Post>
                    ) {
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException("Response code: ${response.code()}"))
                            return
                        }
                        callback.onSuccess(
                            response.body() ?: throw RuntimeException("Body is null")
                        )
                    }
                }
            )
    }

    override fun likeById(id: Int, callback: PostRepository.Callback<Post>) {
        ApiService.service.likeById(id)
            .enqueue(
                object : Callback<Post> {
                    override fun onResponse(
                        call: retrofit2.Call<Post>,
                        response: retrofit2.Response<Post>
                    ) {
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException("Response code: ${response.code()}"))
                            return
                        }
                        callback.onSuccess(
                            response.body() ?: throw RuntimeException("Body is null")
                        )
                    }

                    override fun onFailure(call: retrofit2.Call<Post>, e: Throwable) {
                        callback.onError(RuntimeException(e))
                    }
                }
            )

    }

    override fun unlikeById(id: Int, callback: PostRepository.Callback<Post>) {
        ApiService.service.unlikeById(id)
            .enqueue(
                object : Callback<Post> {
                    override fun onResponse(
                        call: retrofit2.Call<Post>,
                        response: retrofit2.Response<Post>
                    ) {
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException("Response code: ${response.code()}"))
                            return
                        }
                        callback.onSuccess(
                            response.body() ?: throw RuntimeException("Body is null")
                        )
                    }

                    override fun onFailure(call: retrofit2.Call<Post>, e: Throwable) {
                        callback.onError(RuntimeException(e))
                    }
                }
            )
    }

    override fun sharedById(id: Int) {
        //
    }

    override fun removeByID(id: Int, callback: PostRepository.Callback<Unit>) {
        ApiService.service.removeByID(id)
            .enqueue(
                object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException("Response code: ${response.code()}"))
                            return
                        }
                        callback.onSuccess(Unit)
                    }

                    override fun onFailure(call: Call<Unit>, e: Throwable) {
                        callback.onError(Exception(e))
                    }
                }
            )
    }

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        ApiService.service.save(post)
            .enqueue(
                object : Callback<Post> {
                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (!response.isSuccessful) {
                            callback.onError(RuntimeException("Response code: ${response.code()}"))
                            return
                        }
                        callback.onSuccess(
                            response.body() ?: throw RuntimeException("Body is null")
                        )
                    }

                    override fun onFailure(call: Call<Post>, e: Throwable) {
                        callback.onError(Exception(e))
                    }
                }
            )
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