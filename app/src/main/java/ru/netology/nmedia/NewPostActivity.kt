package ru.netology.nmedia

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import ru.netology.nmedia.databinding.ActivityNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val textEdit = intent?.getStringExtra(KEY_TEXT)
        if (textEdit != null) {
            binding.group.visibility = View.VISIBLE
            binding.originalText.text = textEdit
            binding.edit.setText(textEdit)
        }
        binding.edit.focusAndShowKeyboard()
        binding.save.setOnClickListener {
            val inputText = binding.edit.text.toString().trim()
            if (inputText.isNotBlank()) {
                setResult(RESULT_OK, Intent().apply { putExtra(KEY_TEXT, inputText) })
            } else {
                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
            }
            finish()
        }
        binding.closeEdit.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val KEY_TEXT = "post_text"
    }
}

object NewPostContract : ActivityResultContract<String?, String?>() {
    override fun createIntent(context: Context, input: String?) =
        Intent(context, NewPostActivity::class.java).apply {
            putExtra(
                NewPostActivity.KEY_TEXT,
                input
            )
        }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.getStringExtra(NewPostActivity.KEY_TEXT)

}