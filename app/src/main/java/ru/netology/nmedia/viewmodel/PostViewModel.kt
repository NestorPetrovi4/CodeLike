import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import ru.netology.nmedia.db.AppDB
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInRoomImpl

val empty = Post(id = 0, author = "", content = "", published = "", videoURL = "")

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryInRoomImpl(AppDB.getInstance(application).postDAO())

    val data = repository.getAll()
    val edited = MutableLiveData(empty)

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            if (content != it.content) {
                repository.save(it.copy(content = content))
            }
            edited.value = empty
        }
    }

    fun likeById(id: Int) = repository.likeById(id)
    fun sharedById(id: Int) = repository.sharedById(id)
    fun removeById(id: Int) = repository.removeByID(id)

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
        return data.value?.find { it.id == id } ?: empty
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