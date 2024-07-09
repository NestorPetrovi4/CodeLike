package ru.netology.nmedia.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
import ru.netology.nmedia.api.ServiceAPI
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
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDAO,
    private val apiService: ServiceAPI,
    private val appAuth: AppAuth
) : PostRepository {
//    override val data: Flow<List<Post>> = dao.getReadMeAll().map(List<PostEntity>::toDto)
//        .flowOn(Dispatchers.Default)

    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource(apiService)
        }
    ).flow

    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
    }

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
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

    override suspend fun getById(id: Int): Post = apiService.getById(id)

    override suspend fun likeById(id: Int) {
        val post = dao.getById(id)
        var send = false
        if (!post.likedByMe) {
            dao.likeById(id)
            send = true
        }
        try {
            if (send || post.sendServer) {
                apiService.likeById(id)
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
                apiService.unLikeById(id)
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
            if (send || post == null) apiService.removeByID(id)
        } catch (e: NetworkException) {
            throw e
        }
    }

    override suspend fun save(post: Post) {
        dao.save(PostEntity.fromDTO(post))
        try {
            apiService.save(if (post.id == Int.MAX_VALUE) post.copy(id = 0) else post)
            val response = apiService.getAll()
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
                val response = apiService.getNewer(lastId)
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
            val response = apiService.signIn(login, password)
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            val tokenAuth = response.body() ?: throw Exception("Body is null")
            appAuth.setAuth(tokenAuth.id, tokenAuth.token)
        } catch (e: NetworkException) {
            throw e
        }
    }

    override suspend fun signUp(name: String, login: String, password: String) {
        try {
            val response = apiService.signUp(
                name.toRequestBody("text/plain".toMediaType()),
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType())
            )
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            val tokenAuth = response.body() ?: throw Exception("Body is null")
            appAuth.setAuth(tokenAuth.id, tokenAuth.token)
        } catch (e: NetworkException) {
            throw e
        }
    }

    override suspend fun signUp(name: String, login: String, password: String, file: File) {
        try {
            val response = apiService.signUp(
                name.toRequestBody("text/plain".toMediaType()),
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType()),
                MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
            )
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            val tokenAuth = response.body() ?: throw Exception("Body is null")
            appAuth.setAuth(tokenAuth.id, tokenAuth.token)
        } catch (e: NetworkException) {
            throw e
        }
    }

    private suspend fun upload(file: File): Media {
        try {
            val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
            val response = apiService.upload(part)
            if (!response.isSuccessful) throw NetworkException(" Response code: ${response.code()}")
            return response.body() ?: throw UnknownException("Body is null")
        } catch (e: NetworkException) {
            throw e
        }
    }
}