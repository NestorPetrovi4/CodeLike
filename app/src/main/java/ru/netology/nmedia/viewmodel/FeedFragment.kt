package ru.netology.nmedia.viewmodel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {
    val viewModel: PostViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                if (!post.sendServer) {
                    if (appAuth.state.value?.token != null) {
                        viewModel.likeByPost(post)
                    } else {
                        Snackbar.make(
                            binding.root,
                            "${getString(R.string.not_authorized)}",
                            Snackbar.LENGTH_SHORT
                        )
                            .setAction(R.string.sign_in) {
                                findNavController().navigate(R.id.authenticationFragment)
                            }
                            .setAnchorView(binding.add)
                            .show()
                    }
                } else {
                    Snackbar.make(
                        binding.root,
                        "${getString(R.string.error_edit_posts)}",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.retry) {
                            if (post.likedByMe) {
                                viewModel.like(post.id)
                            } else {
                                viewModel.unLike(post.id)
                            }
                        }
                        .setAnchorView(binding.add)
                        .show()
                }
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

            override fun onImageSee(post: Post) {
                post.attachment?.url.let {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_imagePost,
                        Bundle().apply { textArg = viewModel.baseUrlImage + post.attachment?.url })
                }
            }
        }, viewModel.baseUrlImageAvatar, viewModel.baseUrlImage)
        binding.list.adapter = adapter
        binding.navBar.isVisible = false
        binding.ViewNewPosts.isVisible = false
//        viewModel.data.observe(viewLifecycleOwner) { state ->
//            val isNewPost =
//                adapter.currentList.size < state.posts.size && adapter.currentList.isNotEmpty()
//            adapter.submitList(state.posts) {
//                if (isNewPost) binding.list.smoothScrollToPosition(0)
//            }
//            binding.emptyText.isVisible = state.empty
//        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest {
                    adapter.submitData(it)
                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(
                    binding.root,
                    "${getString(R.string.error_getting_the_list_posts)} \n ${state?.errorText ?: ""}",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.retry) {
                        if (state.errorLike >= 0) {
                            viewModel.like(state.errorLike)
                        } else if (state.errorUnLike >= 0) {
                            viewModel.unLike(state.errorUnLike)
                        } else if (state.errorRemove >= 0) {
                            viewModel.removeById(state.errorRemove)
                        } else if (state.errorAddPost != null) {
                            viewModel.changeContentAndSave(state.errorAddPost.content)
                        } else {
//                            viewModel.loadPosts()
                        }
                    }
                    .setAnchorView(binding.add)
                    .show()
            }
        }
//        viewModel.newerCount?.observe(viewLifecycleOwner) {
//            binding.navBar.isVisible = it != 0
//            binding.buttonNotice.text = it.toString()
//            binding.ViewNewPosts.isVisible = it != 0
//        }
        binding.add.setOnClickListener {
            if (appAuth.state.value?.token != null) {
                viewModel.closeEdit()
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                Snackbar.make(
                    binding.root,
                    "${getString(R.string.not_authorized)}",
                    Snackbar.LENGTH_SHORT
                )
                    .setAction(R.string.sign_in) {
                        findNavController().navigate(R.id.authenticationFragment)
                    }
                    .setAnchorView(binding.add)
                    .show()
            }
        }

        binding.buttonNotice.setOnClickListener {
            setRedAll(binding, viewModel)
        }

        binding.ViewNewPosts.setOnClickListener {
            setRedAll(binding, viewModel)
        }

        binding.swiperFresh.setOnRefreshListener {
            binding.navBar.isVisible = false
            //viewModel.loadPosts()
            adapter.refresh()
            //binding.swiperFresh.isRefreshing = false
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest {
                    binding.swiperFresh.isRefreshing =
                        it.refresh is LoadState.Loading || it.append is LoadState.Loading || it.prepend is LoadState.Loading
                }
            }
        }



        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        })

        return binding.root
    }

    private fun setRedAll(binding: FragmentFeedBinding, viewModel: PostViewModel) {
        binding.navBar.isVisible = false
        binding.ViewNewPosts.isVisible = false
        viewModel.setReadAll()
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
