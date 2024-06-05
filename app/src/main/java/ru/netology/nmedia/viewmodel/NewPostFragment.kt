package ru.netology.nmedia.viewmodel

import PostViewModel
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.viewmodel.FeedFragment.Companion.textArg
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
        } ?: run { viewModel.getRepoKey("draft_post") }

        binding.edit.focusAndShowKeyboard()

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data), Snackbar.LENGTH_LONG
                        ).show()

                    }

                    Activity.RESULT_OK -> {
                        val uri = it.data?.data
                        viewModel.changePhoto(uri, uri?.toFile())

                    }
                }
            }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        binding.progress.isVisible = true
                        val inputText = binding.edit.text.toString().trim()
                        if (inputText.isNotBlank()) {
                            viewModel.changeContentAndSave(inputText)
                        } else {
                            Toast.makeText(context, R.string.error_empty_content, Toast.LENGTH_LONG)
                                .show()
                            viewModel.closeEdit()
                        }
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner)

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(2048)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(2048)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.closeEdit.setOnClickListener {
            beforeClose()
        }

        binding.removePhoto.setOnClickListener{
            viewModel.dropPhoto()
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.removeRepoKey("draft_post")
            findNavController().navigateUp()
        }

        viewModel.dataState.observe(viewLifecycleOwner) {
            if (it.errorAddPost != null) {
                findNavController().navigateUp()
            }
        }

        viewModel.repoEntity.observe(viewLifecycleOwner) {
            if (binding.edit.text.isEmpty()) binding.edit.setText(viewModel.repoEntity.value)
        }

        viewModel.photo.observe(viewLifecycleOwner){
            binding.photoContainer.isVisible = it.file != null
            binding.photo.setImageURI(it.uri)
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