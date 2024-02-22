package ru.netology.nmedia.adapter

import PostDiffCallback
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import java.math.RoundingMode
import java.text.DecimalFormat

typealias OnLikeListener = (post: Post) -> Unit
typealias OnShareListener = (post: Post) -> Unit
typealias OnRemoveListener = (post: Post) -> Unit

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onRemove(post: Post) {}
    fun onEdit(post: Post) {}
}

class PostsAdapter(private val onInteractionListener: OnInteractionListener) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            buttonHeart.text = convertDigitMinimizedString(post.likes)
            buttonShare.text = convertDigitMinimizedString(post.shared)
            buttonView.text = convertDigitMinimizedString(post.viewOpen)
            buttonHeart.isChecked = post.likedByMe
            buttonHeart?.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            buttonShare?.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }

    private fun convertDigitMinimizedString(value: Int): String {
        val format = DecimalFormat("#.#")
        format.roundingMode = RoundingMode.DOWN
        if (value < 1000) {
            return value.toString()
        } else if (value < 10000) {
            return (if (value % 1000 == 0) (value / 1000).toString() else format.format((value.toDouble() / 1000))) + "K"
        } else if (value < 1_000_000) {
            return (value / 1000).toString() + "K"
        } else {
            return (if (value % 1000000 == 0) (value / 1000000).toString() else format.format((value.toDouble() / 1000000))) + "M"
        }
    }

}