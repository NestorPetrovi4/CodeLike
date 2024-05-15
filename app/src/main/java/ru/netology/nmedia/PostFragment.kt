package ru.netology.nmedia

import PostViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.FeedFragment.Companion.intArg
import ru.netology.nmedia.FeedFragment.Companion.textArg
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.adapter.load
import ru.netology.nmedia.adapter.loadAvatar
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

class PostFragment() : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
        val binding = CardPostBinding.inflate(inflater, container, false)
        var id = 0
        arguments?.intArg?.let {
            id = it
        } ?: findNavController().navigateUp()

        var post = viewModel.getById(id)

        viewModel.data.observe(viewLifecycleOwner) {
            post = viewModel.getById(id)
            setValueElement(binding, post)
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
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
                        }
                    }.setAnchorView(binding.barrier)
                        .show()
            }
        }

        setValueElement(binding, post)
        binding.apply {
            author.text = post.author
            published.text = post.published
            buttonHeart.isChecked = post.likedByMe
            buttonHeart?.setOnClickListener {
                if (!post.sendServer) {
                    viewModel.likeById(post.id)
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
                        .show()
                }
                buttonHeart.isChecked = post.likedByMe
            }
            buttonShare?.setOnClickListener {
                viewModel.sharedById(post.id)
            }
            group.visibility = if (post.videoURL?.isNotEmpty() == true) View.VISIBLE else View.GONE

            youtubeImage?.setOnClickListener {
                startActivity(FeedFragment.startVideo(post))
            }
            playYoutube.setOnClickListener {
                startActivity(FeedFragment.startVideo(post))
            }
            if (post.attachment?.url.isNullOrEmpty()) {
                attachmentImage.isVisible = false
            } else {
                attachmentImage.isVisible = true
                attachmentImage.load(viewModel.baseUrlImage + post.attachment?.url)
            }
            avatar.loadAvatar(viewModel.baseUrlImageAvatar + post.authorAvatar)

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                viewModel.removeById(post.id)
                                findNavController().navigateUp()
                            }

                            R.id.edit -> {
                                viewModel.edit(post)
                                findNavController().navigate(
                                    R.id.action_postFragment_to_newPostFragment,
                                    Bundle().apply { textArg = post.content })
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
        return binding.root
    }

    private fun setValueElement(binding: CardPostBinding, post: Post) {
        binding.apply {
            content.text = post.content
            buttonHeart.text = PostViewHolder.convertDigitMinimizedString(post.likes)
            buttonShare.text = PostViewHolder.convertDigitMinimizedString(post.shared)
            buttonView.text = PostViewHolder.convertDigitMinimizedString(post.viewOpen)
        }
    }
}