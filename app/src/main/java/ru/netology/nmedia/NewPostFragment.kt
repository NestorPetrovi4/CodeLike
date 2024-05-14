package ru.netology.nmedia

import PostViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
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
            binding.progress.isVisible = false
        } ?: run {viewModel.getRepoKey("draft_post") }

        binding.edit.focusAndShowKeyboard()
        binding.save.setOnClickListener {
            binding.progress.isVisible = true
            val inputText = binding.edit.text.toString().trim()
            if (inputText.isNotBlank()) {
                viewModel.changeContentAndSave(inputText)
                binding.save.isVisible = false
            } else {
                Toast.makeText(context, R.string.error_empty_content, Toast.LENGTH_LONG).show()
                viewModel.closeEdit()
            }
        }
        binding.closeEdit.setOnClickListener {
            beforeClose()
        }
        viewModel.postCreated.observe(viewLifecycleOwner){
            viewModel.removeRepoKey("draft_post")
            findNavController().navigateUp()
        }

        viewModel.dataState.observe(viewLifecycleOwner){
            if (it.errorAddPost != null){
            findNavController().navigateUp()}
        }

        viewModel.repoEntity.observe(viewLifecycleOwner){
            binding.edit.setText(viewModel.repoEntity.value)
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