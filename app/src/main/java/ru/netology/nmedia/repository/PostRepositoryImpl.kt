package ru.netology.nmedia.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import okhttp3.Dispatcher
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
    override val data: Flow<List<Post>> = dao.getReadMeAll().map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

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
            if (send || post.sendServer) {
                ApiService.service.likeById(id)
                dao.sendenServerById(post.id)
            }
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
            if (send || post.sendServer) {
                ApiService.service.unLikeById(id)
                dao.sendenServerById(post.id)
            }
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
            ApiService.service.save(if (post.id == Int.MAX_VALUE) post.copy(id = 0) else post)
            val response = ApiService.service.getAll()
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            val posts = response.body() ?: throw UnknownException("Body is null")
            dao.save(posts.toEntity())
            if (post.id == Int.MAX_VALUE) dao.removeById(Int.MAX_VALUE)
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
    override fun getNewerCount(): Flow<Int> = flow<Int> {
        var lastId = dao.getMaxPostId().firstOrNull()?.id ?: 0
        while (true) {
            delay(30_000)
            try {
                val response = ApiService.service.getNewer(lastId)
                val body = response.body() ?: continue
                dao.save(body.toEntity(false))
                lastId = if (body.isNotEmpty()) body.maxBy { it.id }.id else lastId
//                val notReadMe = flow<List<PostEntity>> {
//                    emit(dao.getReadMeAll(false).single())
//                }
//                val listNotRead = notReadMe.single().size
                emit(dao.getNotReadMeMaxPost().size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
            }
        }

    }
    override suspend fun setReadAll() = dao.setReadAll()

}