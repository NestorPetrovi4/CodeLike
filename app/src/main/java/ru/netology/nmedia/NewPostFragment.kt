package ru.netology.nmedia

import PostViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.FeedFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard

class NewPostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)
        arguments?.textArg?.let {
            binding.group.visibility = View.VISIBLE
            binding.originalText.text = it
            binding.edit.setText(it)
        }
        binding.edit.focusAndShowKeyboard()
        binding.save.setOnClickListener {
            val inputText = binding.edit.text.toString().trim()
            if (inputText.isNotBlank()) {
                viewModel.changeContentAndSave(inputText)
            } else {
                Toast.makeText(context, R.string.error_empty_content, Toast.LENGTH_LONG).show()
                viewModel.closeEdit()
            }
            findNavController().navigateUp()
        }
        binding.closeEdit.setOnClickListener {
            viewModel.closeEdit()
            findNavController().navigateUp()
        }
        return binding.root
    }
}