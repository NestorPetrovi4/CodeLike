package ru.netology.nmedia.viewmodel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.adapter.load
import ru.netology.nmedia.viewmodel.FeedFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentImagePostBinding

@AndroidEntryPoint
class ImagePost() : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentImagePostBinding.inflate(inflater, container, false)
        arguments?.textArg?.let {
            binding.attachmentImage.load(it)
        } ?: findNavController().navigateUp()
        return binding.root
    }
}