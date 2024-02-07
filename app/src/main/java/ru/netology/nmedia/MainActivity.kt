package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            1,
            "Нетология. Университет интернет-профессий будущего",
            "21 мая в 18:36",
            "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            999999, shared = 5
        )
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            textLiked.text = convertDigitMinimizedString(post.likes)
            textShared.text = convertDigitMinimizedString(post.shared)
            textViewOpen.text = convertDigitMinimizedString(post.viewOpen)
            if (post.likedByMe) binding.imageHeart.setImageResource(R.drawable.ic_liked_24)
        }

        binding.imageHeart?.setOnClickListener {
            post.likedByMe = !post.likedByMe
            if (post.likedByMe) {
                post.likes++
                binding.imageHeart.setImageResource(R.drawable.ic_liked_24)
            } else {
                post.likes--
                binding.imageHeart.setImageResource(R.drawable.ic_like_24)
            }
            binding.textLiked.text = convertDigitMinimizedString(post.likes)
        }

        binding.imageShare?.setOnClickListener {
            binding.textShared.text = convertDigitMinimizedString(++post.shared)
        }
    }

    fun convertDigitMinimizedString(value: Int): String {
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