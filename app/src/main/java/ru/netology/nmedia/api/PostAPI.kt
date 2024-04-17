package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
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
    fun getAll(): Call<List<Post>>

    @GET("/api/slow/posts/{id}")
    fun getById(@Path("id") id: Int): Call<Post>

    @POST("/api/slow/posts/{id}/likes")
    fun likeById(@Path("id") id: Int): Call<Post>

    @DELETE("/api/slow/posts/{id}/likes")
    fun unlikeById(@Path("id") id: Int): Call<Post>

    @DELETE("/api/slow/posts/{id}")
    fun removeByID(@Path("id") id: Int): Call<Unit>

    @POST("/api/slow/posts")
    fun save(@Body post: Post): Call<Post>
}

object ApiService {
    val service by lazy { retrofit.create<PostAPI>() }
}