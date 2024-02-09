package ru.netology.nmedia

import PostViewModel
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()
        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                textLiked.text = convertDigitMinimizedString(post.likes)
                textShared.text = convertDigitMinimizedString(post.shared)
                textViewOpen.text = convertDigitMinimizedString(post.viewOpen)
                imageHeart.setImageResource(
                    if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24
                )
            }
        }

        binding.imageHeart?.setOnClickListener {
            viewModel.like()
        }

        binding.imageShare?.setOnClickListener {
            viewModel.shared()
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