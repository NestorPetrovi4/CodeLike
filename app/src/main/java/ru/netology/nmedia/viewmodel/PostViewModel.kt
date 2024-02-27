import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository

val empty = Post(id = 0, author = "", content = "", published = "", videoURL = "")

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    val edited = MutableLiveData(empty)

    fun changeContentAndSave(content: String){
        edited.value?.let {
            if(content != it.content){
                repository.save(it.copy(content = content))
            }
            edited.value = empty
        }
    }
    fun likeById(id: Int) = repository.likeById(id)
    fun sharedById(id: Int) = repository.sharedById(id)
    fun removeById(id: Int) = repository.removeByID(id)

    fun edit(post: Post){
        edited.value = post
    }

    fun closeEdit(){
        edited.value = empty
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