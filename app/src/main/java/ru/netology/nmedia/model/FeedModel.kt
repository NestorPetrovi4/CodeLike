package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false
)

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val errorText: String? = "",
    val errorLike: Int = -1,
    val errorUnLike: Int = -1,
    val errorRemove: Int = -1,
    val errorAddPost: Post? = null
)