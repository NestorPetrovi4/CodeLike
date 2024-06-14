package ru.netology.nmedia.viewmodel

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAuthenticationBinding
import ru.netology.nmedia.viewmodel.FeedFragment.Companion.textArg

class AuthenticationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: AuthViewModel by viewModels(ownerProducer = ::requireParentFragment)
        val binding = FragmentAuthenticationBinding.inflate(inflater, container, false)
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
        arguments?.textArg?.let {
            binding.name.isVisible = true
            binding.confirmPassword.isVisible = true
            binding.signUp.isVisible = true
            binding.signIn.isVisible = false
            binding.photoContainer.isVisible = true
            binding.pickPhoto.isVisible = true
        }
        binding.signIn.setOnClickListener {
            viewModel.signIn(binding.user.text.toString(), binding.password.text.toString())
        }

        binding.signUp.setOnClickListener {
            binding.password.text.isNotBlank().let {
                if(binding.password.text.toString() == binding.confirmPassword.text.toString()) {
                    viewModel.signUp(binding.name.text.toString(), binding.user.text.toString(),
                        binding.password.text.toString())
                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(
                    binding.root,
                    "${getString(R.string.channel_remote_name)} \n ${state?.errorText ?: ""}",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.close) {
                        viewModel.dropState()
                    }
                    .setAnchorView(binding.signIn)
                    .show()
            }
        }

        viewModel.auth.observe(viewLifecycleOwner) {
            if (it?.token != null) {
                findNavController().navigateUp()
            }
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.dropPhoto()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            binding.photoContainer.isVisible = it.file != null
            binding.photo.setImageURI(it.uri)
            binding.removePhoto.isVisible = binding.photoContainer.isVisible
            binding.pickPhoto.isVisible = !binding.photoContainer.isVisible
        }

        return binding.root
    }

}