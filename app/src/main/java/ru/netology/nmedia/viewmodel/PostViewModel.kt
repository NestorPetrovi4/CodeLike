import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import ru.netology.nmedia.db.AppDB
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.lang.Exception

val empty = Post(id = 0, author = "", content = "", published = "", videoURL = "")

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDB.getInstance(application).postDAO())
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.postValue(FeedModel(loading = true))
        repository.getAll(
            object : PostRepository.Callback<List<Post>> {
                override fun onSuccess(posts: List<Post>) {
                    _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
                }

                override fun onError(exception: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            }
        )
    }

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            if (content != it.content) {
                repository.save(it.copy(content = content), object : PostRepository.Callback<Post> {
                    override fun onSuccess(newPost: Post) {
                        _postCreated.postValue(Unit)
                    }
                    override fun onError(exception: Exception) {
                        _data.postValue(_data.value?.copy(loading = false, error = true))
                    }
                })
                edited.value = empty
            }
        }
    }

    fun likeById(id: Int) {
        val post = getById(id).let { it.copy(likedByMe = !it.likedByMe) }
        _data.postValue(
            _data.value?.copy(loading = true, posts = _data.value?.posts.orEmpty()
                .map { if (it.id != id) it else post })
        )
        if (post.likedByMe)
            repository.likeById(id, object : PostRepository.Callback<Post> {
                override fun onSuccess(newPost: Post) {
                    _data.postValue(
                        _data.value?.copy(loading = false, posts = _data.value?.posts.orEmpty()
                            .map { if (it.id != id) it else newPost })
                    )
                }

                override fun onError(exception: Exception) {
                    _data.postValue(_data.value?.copy(loading = false, error = true))
                }
            })
        else repository.unlikeById(id, object : PostRepository.Callback<Post> {
            override fun onSuccess(newPost: Post) {
                _data.postValue(
                    _data.value?.copy(loading = false, posts = _data.value?.posts.orEmpty()
                        .map { if (it.id != id) it else newPost })
                )
            }

            override fun onError(exception: Exception) {
                _data.postValue(_data.value?.copy(loading = false, error = true))
            }
        })

    }

    fun sharedById(id: Int) = repository.sharedById(id)
    fun removeById(id: Int) {
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id })
        )
        repository.removeByID(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(unit: Unit) {}
            override fun onError(exception: Exception) {
                _data.postValue(_data.value?.copy(posts = old, error = true))
            }
        })
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun closeEdit() {
        edited.value = empty
    }

    fun addRepoValue(key: String, value: String) {
        repository.addRepoValue(key, value)
    }

    fun getById(id: Int): Post {
        return data.value?.posts?.find { it.id == id } ?: empty
    }

    fun removeRepoKey(key: String) {
        repository.removeRepoKey(key)
    }

    fun getRepoKey(key: String) = repository.getRepoKey(key)
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}