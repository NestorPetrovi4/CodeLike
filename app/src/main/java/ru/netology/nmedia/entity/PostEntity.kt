package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val author: String,
    val published: String,
    val content: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shared: Int = 0,
    val viewOpen: Int = 0,
    val videoURL: String? = ""
) {
    fun toDTO() = Post(id, author, published, content, likes, likedByMe, shared, viewOpen, videoURL)

    companion object {
        fun fromDTO(post: Post) =
            PostEntity(
                post.id,
                post.author,
                post.published,
                post.content,
                post.likes,
                post.likedByMe,
                post.shared,
                post.viewOpen,
                post.videoURL
            )
    }
}

@Entity
class RepoEntity(
    @PrimaryKey
    val id: String,
    val value: String
)