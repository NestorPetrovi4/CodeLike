package ru.netology.nmedia

import PostViewModel
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import org.jetbrains.annotations.ApiStatus.Internal
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()
        val newPostLauncher = registerForActivityResult(NewPostContract) { result ->
            if (result == null) {
                viewModel.closeEdit()
                return@registerForActivityResult
            } else
                viewModel.changeContentAndSave(result)
        }
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                viewModel.sharedById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onYoutubeSee(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.videoURL))
                val seeVideo = Intent.createChooser(intent, post.videoURL)
                startActivity(seeVideo)
            }
        })
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            val isNewPost =
                adapter.currentList.size < posts.size && adapter.currentList.isNotEmpty()
            adapter.submitList(posts) {
                if (isNewPost) binding.list.smoothScrollToPosition(0)
            }
        }
        viewModel.edited.observe(this) { post ->
            if (post.id != 0) {
                newPostLauncher.launch(post.content)
            }
        }
        binding.add.setOnClickListener {
            newPostLauncher.launch(null)
        }
    }
}