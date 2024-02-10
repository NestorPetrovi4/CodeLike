import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    fun likeById(id: Int) = repository.likeById(id)
    fun sharedById(id: Int) = repository.sharedById(id)
}

class PostDiffCallback : DiffUtil.ItemCallback<Post> (){
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}