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
import java.io.IOException
import java.lang.Exception
import kotlin.concurrent.thread

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
        thread {
            _data.postValue(FeedModel(loading = true))
            try {
                val posts = repository.getAll()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                FeedModel(error = true)
            }.also(_data::postValue)
        }
    }

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            thread {
                if (content != it.content) {
                    repository.save(it.copy(content = content))
                    _postCreated.postValue(Unit)
                }
            }
            edited.value = empty
        }
    }

    fun likeById(id: Int) {
        thread {
            val post = getById(id).copy(likedByMe = true)
            _data.postValue(
                _data.value?.copy(loading = true, posts = _data.value?.posts.orEmpty()
                    .map { if (it.id != id) it else post })
            )
            try {
                val newPost = if (post.likedByMe)
                    repository.likeById(id)
                else repository.unlikeById(id)
                _data.postValue(
                    _data.value?.copy(loading = false, posts = _data.value?.posts.orEmpty()
                        .map { if (it.id != id) it else newPost })
                )
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(loading = false, error = true))
            }
        }
    }

    fun sharedById(id: Int) = repository.sharedById(id)
    fun removeById(id: Int) {
        thread {
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id })
            )
            try {
                repository.removeByID(id)
            } catch (e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
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