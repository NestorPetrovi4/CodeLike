package ru.netology.nmedia.dto
data class Post(
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
    val attachment: Attachment? = null,
    val sendServer: Boolean = false
)
