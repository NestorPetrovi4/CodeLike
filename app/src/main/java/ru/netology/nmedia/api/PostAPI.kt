package ru.netology.nmedia.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nmedia.db.Token
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken

interface ServiceAPI {
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

    @Multipart
    @POST("/api/slow/media")
    suspend fun upload(@Part file: MultipartBody.Part): Response<Media>

    @FormUrlEncoded
    @POST("/api/slow/users/authentication")
    suspend fun signIn(@Field("login") login: String, @Field("pass") password: String): Response<Token>

    @Multipart
    @POST("/api/slow/users/registration")
    suspend fun signUp(@Part("name") name: RequestBody,
                       @Part("login") login: RequestBody,
                       @Part("pass") password: RequestBody): Response<Token>

    @Multipart
    @POST("/api/slow/users/registration")
    suspend fun signUp(@Part("name") name: RequestBody,
                       @Part("login") login: RequestBody,
                       @Part("pass") password: RequestBody,
                       @Part media: MultipartBody.Part): Response<Token>
    @POST("/api/slow/users/push-tokens")
    suspend fun saveToken(@Body token: PushToken): Response<Unit>
}