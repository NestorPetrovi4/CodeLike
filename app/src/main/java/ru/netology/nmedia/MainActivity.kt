package ru.netology.nmedia

import PostViewModel
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
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
        })
        binding.group.visibility = View.GONE
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
                binding.group.visibility = View.VISIBLE
                binding.originalText.text = post.content
                binding.edit.setText(post.content)
                binding.edit.focusAndShowKeyboard()
            }
        }

        binding.closeEdit.setOnClickListener {
            binding.group.visibility = View.GONE
            binding.edit.setText("")
            binding.edit.clearFocus()
            AndroidUtils.hideKeyboard(it)
            viewModel.closeEdit()
        }

        binding.save.setOnClickListener {
            binding.group.visibility = View.GONE
            val inputText = binding.edit.text.toString().trim()
            if (inputText.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.changeContentAndSave(inputText)
            binding.edit.setText("")
            binding.edit.clearFocus()
            AndroidUtils.hideKeyboard(it)
        }
    }
}