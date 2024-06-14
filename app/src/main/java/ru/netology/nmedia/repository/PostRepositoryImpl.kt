package ru.netology.nmedia.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.RepoEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import java.io.File

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
                emit(dao.getNotReadMeMaxPost().size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
            }
        }

    }

    override suspend fun setReadAll() = dao.setReadAll()
    override suspend fun saveWithAttachment(post: Post, file: File) {
        save(post.copy(attachment = Attachment(upload(file).id, "", AttachmentType.IMAGE)))
    }

    override suspend fun signIn(login: String, password: String) {
        try {
            val response = ApiService.service.signIn(login, password)
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            val tokenAuth = response.body() ?: throw Exception("Body is null")
            AppAuth.getInstance().setAuth(tokenAuth.id, tokenAuth.token)
        } catch (e: NetworkException) {
            throw e
        }
    }

    override suspend fun signUp(name: String, login: String, password: String) {
        try {
            val response = ApiService.service.signUp(name.toRequestBody("text/plain".toMediaType()),
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType()))
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            val tokenAuth = response.body() ?: throw Exception("Body is null")
            AppAuth.getInstance().setAuth(tokenAuth.id, tokenAuth.token)
        } catch (e: NetworkException) {
            throw e
        }
    }

    override suspend fun signUp(name: String, login: String, password: String, file: File) {
        try {
            val response = ApiService.service.signUp(name.toRequestBody("text/plain".toMediaType()),
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType()),
                MultipartBody.Part.createFormData("file", file.name, file.asRequestBody()))
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            val tokenAuth = response.body() ?: throw Exception("Body is null")
            AppAuth.getInstance().setAuth(tokenAuth.id, tokenAuth.token)
        } catch (e: NetworkException) {
            throw e
        }
    }

    private suspend fun upload(file: File): Media {
        try {
            val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
            val response = ApiService.service.upload(part)
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            return response.body() ?: throw UnknownException("Body is null")
        } catch (e: NetworkException) {
            throw e
        }
    }
}