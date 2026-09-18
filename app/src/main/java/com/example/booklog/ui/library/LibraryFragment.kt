package com.example.booklog.ui.library

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.booklog.R
import com.example.booklog.data.repository.ReviewRepository
import com.example.booklog.databinding.FragmentLibraryBinding
import kotlinx.coroutines.launch

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ReviewRepository
    private lateinit var adapter: LibraryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        repository = ReviewRepository(requireContext())

        adapter = LibraryAdapter { reviewId ->

            findNavController().navigate(
                R.id.reviewDetailFragment,
                bundleOf("reviewId" to reviewId)
            )
        }

        binding.rvLibrary.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvLibrary.adapter = adapter

        loadReviews()
    }

    private fun loadReviews() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.observeAll().collect { adapter.submitList(it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
