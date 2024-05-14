package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.RepoEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException

class PostRepositoryImpl(private val dao: PostDAO) : PostRepository {
    override val data: LiveData<List<Post>> = dao.getAll().map(List<PostEntity>::toDto)

    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
    }

    override suspend fun getAll() {
        try {
            val response = ApiService.service.getAll()
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            val posts = response.body() ?: throw UnknownException("Body is null")
            dao.save(posts.toEntity())
        } catch (e: UnknownException) {
            throw e
        } catch (e: NetworkException) {
            throw e
        } catch (e: Exception) {
            throw UnknownException("${e.message} Other error API")
        }
    }

    override suspend fun getById(id: Int): Post = ApiService.service.getById(id)

    override suspend fun likeById(id: Int) {
        val post = dao.getById(id)
        var send = false
        if (!post.likedByMe) {
            dao.likeById(id)
            send = true
        }
        try {
            if (send || post.sendServer) ApiService.service.likeById(id)
        } catch (e: NetworkException) {
            throw e
        }
    }

    override suspend fun unLikeById(id: Int) {
        val post = dao.getById(id)
        var send = false
        if (post.likedByMe) {
            dao.unLikeById(id)
            send = true
        }
        try {
            if (send || post.sendServer) ApiService.service.unLikeById(id)
        } catch (e: NetworkException) {
            throw e
        }
    }

    override suspend fun sharedById(id: Int) {}

    override suspend fun removeByID(id: Int) {
        val post = dao.getById(id)
        var send = false
        if (post != null) {
            dao.removeById(id)
            send = true
        }
        try {
            if (send || post == null) ApiService.service.removeByID(id)
        } catch (e: NetworkException) {
            throw e
        }
    }

    override suspend fun save(post: Post) {
        dao.save(PostEntity.fromDTO(post))
        try {
            ApiService.service.save(if(post.id == -1) post.copy(id =0) else post)
            val response = ApiService.service.getAll()
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            val posts = response.body() ?: throw UnknownException("Body is null")
            dao.save(posts.toEntity())
            if (post.id == -1) dao.removeById(-1)
        } catch (e: NetworkException) {
            throw e
        }
    }

    override fun getBASE_URL(): String {
        return BASE_URL
    }

    override suspend fun addRepoValue(key: String, value: String) {
        dao.addRepoValue(RepoEntity(key, value))
    }

    override suspend fun removeRepoKey(key: String) {
        dao.removeRepoKey(key)
    }

    override suspend fun getRepoKey(key: String) = dao.getRepoKey(key)
}