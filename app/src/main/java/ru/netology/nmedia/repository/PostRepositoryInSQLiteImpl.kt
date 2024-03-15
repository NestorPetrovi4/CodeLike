package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.dto.Post

class PostRepositoryInSQLiteImpl(private val dao: PostDAO) : PostRepository {
    private var posts = emptyList<Post>()
        private set(value) {
            field = value
            data.value = value
        }
    private val data = MutableLiveData(posts)

    init {
        posts = dao.getAll()
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Int) {
        dao.likeById(id)
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likedByMe = !it.likedByMe,
                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
            )
        }
    }

    override fun sharedById(id: Int) {
        dao.sharedById(id)
        posts = posts.map {
            if (it.id != id) it else it.copy(shared = it.shared + 1)
        }
    }

    override fun removeByID(id: Int) {
        dao.removeById(id)
        posts = posts.filter { it.id != id }
    }

    override fun save(post: Post) {
        val id = post.id
        val saved = dao.save(post)
        posts = if (id == 0) {
            listOf(saved) + posts
        } else {
            posts.map { if (it.id != id) it else saved }
        }
    }

    override fun addRepoValue(key: String, value: String) {
        dao.addRepoValue(key, value)
    }

    override fun removeRepoKey(key: String) {
        dao.removeRepoKey(key)
    }

    override fun getRepoKey(key: String) = dao.getRepoKey(key)
}