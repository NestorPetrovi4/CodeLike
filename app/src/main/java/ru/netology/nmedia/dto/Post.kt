package ru.netology.nmedia.dto
data class Post(
    val id: Int,
    val author: String,
    val published: String,
    val content: String,
    var likes: Int = 0,
    var likedByMe: Boolean = false,
    var shared: Int = 0,
    var viewOpen: Int = 0
)
