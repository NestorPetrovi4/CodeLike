package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nmedia.dto.Attachment
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
    val videoURL: String? = "",
    val authorAvatar: String? = "",
    @TypeConverters
    val attachment: AttachmentEntity?
) {
    fun toDTO() = Post(
        id,
        author,
        published,
        content,
        likes,
        likedByMe,
        shared,
        viewOpen,
        videoURL,
        authorAvatar,
        null
       // attachment.toDTO()
    )

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
                post.videoURL,
                post.authorAvatar,
                null
              //  AttachmentEntity.fromDTO(post.attachment)
            )
    }
}

@Entity
class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val url: String = "",
    val description: String = "",
    val type: String = ""
) {
    fun toDTO() = Attachment(
        url,
        description,
        type
    )

    companion object {
        fun fromDTO(attachment: Attachment) =
            attachment?.let {
                AttachmentEntity(
                    it.url,
                    it.description,
                    it.type
                )
            }
    }
}

@Entity
class RepoEntity(
    @PrimaryKey
    val id: String,
    val value: String
)