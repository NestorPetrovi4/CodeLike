package ru.netology.nmedia

import PostViewModel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post

class FeedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
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
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply { textArg = post.content })
            }

            override fun onYoutubeSee(post: Post) {
                startActivity(startVideo(post))
            }
        }, viewModel.baseUrlImageAvatar, viewModel.baseUrlImage)
        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            val isNewPost =
                adapter.currentList.size < state.posts.size && adapter.currentList.isNotEmpty()
            adapter.submitList(state.posts) {
                if (isNewPost) binding.list.smoothScrollToPosition(0)
            }
            binding.errorGroup.isVisible = state.error
            binding.progress.isVisible = state.loading
            binding.emptyText.isVisible = state.empty
        }
        binding.add.setOnClickListener {
            viewModel.closeEdit()
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.retry.setOnClickListener{
            viewModel.loadPosts()
        }
        binding.swiperFresh.setOnRefreshListener{
            viewModel.loadPosts()
            binding.swiperFresh.isRefreshing = false
        }

        return binding.root
    }

    companion object {
        private const val KEY_TEXT = "KEY_TEXT"
        var Bundle.textArg: String?
            set(value) = putString(KEY_TEXT, value)
            get() = getString(KEY_TEXT)
        var Bundle.intArg: Int
            set(value) = putInt("KEY_INT", value)
            get() = getInt("KEY_INT")

        fun startVideo(post: Post): Intent {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.videoURL))
            return Intent.createChooser(intent, post.videoURL)
        }
    }
}
