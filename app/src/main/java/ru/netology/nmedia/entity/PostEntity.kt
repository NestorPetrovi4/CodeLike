package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.AttachmentType

@Entity
class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val author: String,
    val authorId: Int,
    val published: String,
    val content: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shared: Int = 0,
    val viewOpen: Int = 0,
    val videoURL: String? = "",
    val authorAvatar: String? = "",
    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    val sendServer: Boolean = false,
    val readMe: Boolean = true
) {
    fun toDTO() = Post(
        id,
        author,
        authorId,
        published,
        content,
        likes,
        likedByMe,
        shared,
        viewOpen,
        videoURL,
        authorAvatar,
        attachment?.toDTO(),
        sendServer
    )

    companion object {
        fun fromDTO(post: Post, readMe: Boolean = true) =
            PostEntity(
                post.id,
                post.author,
                post.authorId,
                post.published,
                post.content,
                post.likes,
                post.likedByMe,
                post.shared,
                post.viewOpen,
                post.videoURL,
                post.authorAvatar,
                post.attachment?.let { AttachmentEmbeddable.fromDTO(post.attachment) },
                post.sendServer,
                readMe
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDTO)
fun List<Post>.toEntity(readMe: Boolean = true): List<PostEntity> =
    map { PostEntity.fromDTO(it, readMe) }

@Entity
class AttachmentEmbeddable(
    val url: String,
    val description: String,
    val type: AttachmentType
) {
    fun toDTO() = Attachment(
        url,
        description,
        type
    )

    companion object {
        fun fromDTO(attachment: Attachment) =
            attachment?.let {
                AttachmentEmbeddable(
                    it.url,
                    it.description,
                    it.type
                )
            }
    }
}

@Entity
class RepoEntity(
    @PrimaryKey()
    val id: String,
    val value: String
)