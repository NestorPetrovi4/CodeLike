import android.app.Application
import android.net.Uri
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDB
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import ru.netology.nmedia.viewmodel.AuthViewModel
import java.io.File

val empty = Post(id = 0, author = "", authorId = 0, content = "", published = "")

val noPhoto = PhotoModel()

@ExperimentalCoroutinesApi
class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDB.getInstance(application).postDAO())
    val baseUrlImageAvatar = repository.getBASE_URL() + "/avatars/"
    val baseUrlImage = repository.getBASE_URL() + "/media/"

    val data: LiveData<FeedModel> = AppAuth.getInstance()
        .state
        .flatMapLatest { auth ->
            repository.data
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownerByMe = it.authorId == auth?.id) },
                        empty = posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

private val _dataState = MutableLiveData(FeedModelState())
val dataState: LiveData<FeedModelState>
    get() = _dataState

private val _photo = MutableLiveData(noPhoto)
val photo: LiveData<PhotoModel>
    get() = _photo


private val _newerCount = MutableLiveData(data.switchMap {
    repository.getNewerCount().asLiveData(Dispatchers.Default)
})
val newerCount: LiveData<Int>?
    get() = _newerCount.value

val edited = MutableLiveData(empty)

private val _postCreated = SingleLiveEvent<Unit>()
val postCreated: LiveData<Unit>
    get() = _postCreated

private val _repoEntity = MutableLiveData("")
val repoEntity: LiveData<String>
    get() = _repoEntity

init {
    loadPosts()
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

fun likeById(id: Int) {
    val post = getById(id).let { it.copy(likedByMe = !it.likedByMe) }
    if (post.likedByMe) {
        like(id)
    } else {
        unLike(id)
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

fun getById(id: Int): Post {
    return data.value?.posts?.find { it.id == id } ?: empty
}

fun removeRepoKey(key: String) = viewModelScope.launch { repository.removeRepoKey(key) }

fun getRepoKey(key: String) =
    viewModelScope.launch { _repoEntity.value = repository.getRepoKey(key) }

fun setReadAll() = viewModelScope.launch {
    repository.setReadAll()
    _newerCount.value = repository.getNewerCount().asLiveData(Dispatchers.Default)
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