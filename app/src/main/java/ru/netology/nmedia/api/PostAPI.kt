package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

private const val BASE_URL = BuildConfig.BASE_URL
private val retrofit = Retrofit.Builder()
    .client(
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                if (BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    )
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface PostAPI {
    @GET("/api/slow/posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("/api/slow/posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Int): Response<List<Post>>

    @GET("/api/slow/posts/{id}")
    suspend fun getById(@Path("id") id: Int): Post

    @POST("/api/slow/posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Int): Post

    @DELETE("/api/slow/posts/{id}/likes")
    suspend fun unLikeById(@Path("id") id: Int): Post

    @DELETE("/api/slow/posts/{id}")
    suspend fun removeByID(@Path("id") id: Int): Unit

    @POST("/api/slow/posts")
    suspend fun save(@Body post: Post): Post
}

object ApiService {
    val service by lazy { retrofit.create<PostAPI>() }
}