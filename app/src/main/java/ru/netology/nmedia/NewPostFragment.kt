package ru.netology.nmedia

import PostViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.FeedFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard

class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
    private lateinit var binding: FragmentNewPostBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            beforeClose()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewPostBinding.inflate(inflater, container, false)
        arguments?.textArg?.let {
            binding.group.visibility = View.VISIBLE
            binding.originalText.text = it
            binding.edit.setText(it)
        } ?: run {binding.edit.setText(viewModel.getRepoKey("draft_post")) }

        binding.edit.focusAndShowKeyboard()
        binding.save.setOnClickListener {
            val inputText = binding.edit.text.toString().trim()
            if (inputText.isNotBlank()) {
                viewModel.changeContentAndSave(inputText)
                viewModel.removeRepoKey("draft_post")
            } else {
                Toast.makeText(context, R.string.error_empty_content, Toast.LENGTH_LONG).show()
                viewModel.closeEdit()
            }
            findNavController().navigateUp()
        }
        binding.closeEdit.setOnClickListener {
            beforeClose()
        }
        return binding.root
    }

    fun beforeClose() {
        if (viewModel.edited.value?.id != 0) {
            viewModel.closeEdit()
        } else {
            viewModel.addRepoValue("draft_post", binding.edit.text.toString())
        }
        findNavController().navigateUp()
    }
}