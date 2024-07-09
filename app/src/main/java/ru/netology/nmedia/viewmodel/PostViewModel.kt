package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.switchMap
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

val empty = Post(id = 0, author = "", authorId = 0, content = "", published = "")

val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {
    val baseUrlImageAvatar = repository.getBASE_URL() + "/avatars/"
    val baseUrlImage = repository.getBASE_URL() + "/media/"

//    val data: LiveData<FeedModel> = appAuth
//        .state
//        .flatMapLatest { auth ->
//            repository.data
//                .map { posts ->
//                    FeedModel(
//                        posts.map { it.copy(ownerByMe = it.authorId == auth?.id) },
//                        empty = posts.isEmpty()
//                    )
//                }
//        }.asLiveData(Dispatchers.Default)

    val data: Flow<PagingData<Post>> = appAuth.state
        .flatMapLatest {auth ->
        repository.data
            .map { posts ->
                posts.map { it.copy(ownerByMe = it.authorId == auth?.id) }
            }
    }.flowOn(Dispatchers.Default)


    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

//    private val _newerCount = MutableLiveData(data.switchMap {
//        repository.getNewerCount().asLiveData(Dispatchers.Default)
//    })

//    val newerCount: LiveData<Int>?
//        get() = _newerCount.value

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _repoEntity = MutableLiveData("")
    val repoEntity: LiveData<String>
        get() = _repoEntity

    init {
        //loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.getAll()
            _dataState.postValue(FeedModelState())

        } catch (e: Exception) {
            _dataState.postValue(FeedModelState(error = true, errorText = e.message))
        }
    }

    fun changeContentAndSave(content: String, repeat: Boolean = false) = viewModelScope.launch {
        edited.value?.let {
            if (content != it.content || repeat) {
                try {
                    _postCreated.postValue(Unit)
                    _dataState.postValue(FeedModelState(loading = true))
                    val copyPost =
                        it.copy(content = content, id = if (it.id == 0) Int.MAX_VALUE else it.id)
                    when (_photo.value) {
                        noPhoto -> repository.save(copyPost)
                        else -> _photo.value?.file?.let { file ->
                            repository.saveWithAttachment(copyPost, file)
                        }
                    }
                    edited.value = empty
                    _dataState.postValue(FeedModelState())
                    _photo.value = noPhoto
                } catch (e: Exception) {
                    _dataState.postValue(
                        FeedModelState(
                            error = true,
                            errorAddPost = it.copy(content = content),
                            errorText = e.message
                        )
                    )
                }
            }
        }
    }

    fun likeByPost(postId: Post) {
        val post = postId.let { it.copy(likedByMe = !it.likedByMe) }
        if (post.likedByMe) {
            like(post.id)
        } else {
            unLike(post.id)
        }
    }

    fun like(id: Int) = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.likeById(id)
            _dataState.postValue(FeedModelState())

        } catch (e: Exception) {
            _dataState.postValue(
                FeedModelState(
                    error = true,
                    errorLike = id,
                    errorText = e.message
                )
            )
        }
    }

    fun unLike(id: Int) = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.unLikeById(id)
            _dataState.postValue(FeedModelState())

        } catch (e: Exception) {
            _dataState.postValue(
                FeedModelState(
                    error = true,
                    errorUnLike = id,
                    errorText = e.message
                )
            )
        }
    }


    fun sharedById(id: Int) = viewModelScope.launch { repository.sharedById(id) }
    fun removeById(id: Int) = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.removeByID(id)
            _dataState.postValue(FeedModelState())

        } catch (e: Exception) {
            _dataState.postValue(
                FeedModelState(
                    error = true,
                    errorRemove = id,
                    errorText = e.message
                )
            )
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun closeEdit() {
        edited.value = empty
    }

    fun addRepoValue(key: String, value: String) =
        viewModelScope.launch { repository.addRepoValue(key, value) }

    fun removeRepoKey(key: String) = viewModelScope.launch { repository.removeRepoKey(key) }

    fun getRepoKey(key: String) =
        viewModelScope.launch { _repoEntity.value = repository.getRepoKey(key) }

    fun setReadAll() = viewModelScope.launch {
        repository.setReadAll()
//        _newerCount.value = repository.getNewerCount().asLiveData(Dispatchers.Default)
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun dropPhoto() {
        _photo.value = noPhoto
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}