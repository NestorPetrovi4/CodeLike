package ru.netology.nmedia.adapter

import PostDiffCallback
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import java.math.RoundingMode
import java.text.DecimalFormat

typealias OnLikeListener = (post: Post) -> Unit
typealias OnShareListener = (post: Post) -> Unit

class PostsAdapter(
    private val onLikeListener: OnLikeListener,
    private val onShareListener: OnShareListener
) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onLikeListener, onShareListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onLikeListener: OnLikeListener,
    private val onShareListener: OnShareListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            textLiked.text = convertDigitMinimizedString(post.likes)
            textShared.text = convertDigitMinimizedString(post.shared)
            textViewOpen.text = convertDigitMinimizedString(post.viewOpen)
            imageHeart.setImageResource(
                if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24
            )
            imageHeart?.setOnClickListener {
                onLikeListener(post)
            }
            imageShare?.setOnClickListener {
                onShareListener(post)
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