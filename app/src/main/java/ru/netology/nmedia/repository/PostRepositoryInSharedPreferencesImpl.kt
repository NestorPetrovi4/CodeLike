import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository

class PostRepositoryInSharedPreferencesImpl(private val context: Context) : PostRepository {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val key = "posts"
    private var nextId = 1
    private var posts = emptyList<Post>()
        private set(value) {
            field = value
            data.value = value
            sync()
        }
    private val data = MutableLiveData(posts)

    init {
        prefs.getString(key, null)?.let {
            posts = gson.fromJson(it, type)
            nextId = posts.maxOfOrNull { it -> it.id }?.inc() ?: 1
        }
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Int) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likedByMe = !it.likedByMe,
                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
            )
        }
    }

    override fun sharedById(id: Int) {
        posts = posts.map {
            if (it.id != id) it else it.copy(shared = it.shared + 1)
        }
    }

    override fun removeByID(id: Int) {
        posts = posts.filter { it.id != id }
    }

    override fun save(post: Post) {
        posts = if (post.id == 0) {
            listOf(post.copy(id = nextId++, published = "Now", author = "Netology")) + posts
        } else {
            posts.map {
                if (it.id != post.id) it else it.copy(content = post.content)
            }
        }
    }

    override fun addRepoValue(key: String, value: String) {}
    override fun removeRepoKey(key: String) {}
    override fun getRepoKey(key: String) = ""

    private fun sync() {
        with(prefs.edit()) {
            putString(key, gson.toJson(posts))
            apply()
        }
    }
}