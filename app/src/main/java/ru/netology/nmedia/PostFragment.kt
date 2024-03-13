package ru.netology.nmedia

import PostViewModel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.FeedFragment.Companion.intArg
import ru.netology.nmedia.FeedFragment.Companion.textArg
import ru.netology.nmedia.adapter.PostViewHolder
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
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            buttonHeart.text = PostViewHolder.convertDigitMinimizedString(post.likes)
            buttonShare.text = PostViewHolder.convertDigitMinimizedString(post.shared)
            buttonView.text = PostViewHolder.convertDigitMinimizedString(post.viewOpen)
            buttonHeart.isChecked = post.likedByMe
            buttonHeart?.setOnClickListener {
                viewModel.likeById(post.id)
                post = viewModel.getById(id)
                buttonHeart.text = PostViewHolder.convertDigitMinimizedString(post.likes)
            }
            buttonShare?.setOnClickListener {
                viewModel.sharedById(post.id)
                post = viewModel.getById(id)
                buttonShare.text = PostViewHolder.convertDigitMinimizedString(post.shared)
            }
            group.visibility = if (post.videoURL.isNotEmpty()) View.VISIBLE else View.GONE

            youtubeImage?.setOnClickListener {
                startActivity(FeedFragment.startVideo(post))
            }
            playYoutube.setOnClickListener {
                startActivity(FeedFragment.startVideo(post))
            }

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
}