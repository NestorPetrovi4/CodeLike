package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.dao.PostDAO
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

class PostRepositoryInRoomImpl(private val dao: PostDAO) : PostRepository {

    override fun getAll(): LiveData<List<Post>> = dao.getAll().map { list ->
        list.map { it.toDTO() }
    }

    override fun likeById(id: Int) {
        dao.likeById(id)
    }

    override fun sharedById(id: Int) {
        dao.sharedById(id)
    }

    override fun removeByID(id: Int) {
        dao.removeById(id)
    }

    override fun save(post: Post) {
        dao.save(PostEntity.fromDTO(post))
    }

    override fun addRepoValue(key: String, value: String) {
        dao.addRepoValue(key, value)
    }

    override fun removeRepoKey(key: String) {
        dao.removeRepoKey(key)
    }

    override fun getRepoKey(key: String) = dao.getRepoKey(key)
}