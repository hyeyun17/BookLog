package com.example.booklog.ui.timeline

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklog.R
import com.example.booklog.data.repository.ReviewRepository
import com.example.booklog.databinding.FragmentTimelineBinding
import kotlinx.coroutines.launch

class TimelineFragment : Fragment(R.layout.fragment_timeline) {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ReviewRepository
    private lateinit var adapter: TimelineAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTimelineBinding.bind(view)

        repository = ReviewRepository(requireContext())

        adapter = TimelineAdapter { reviewId ->

            findNavController().navigate(
                R.id.reviewDetailFragment,
                bundleOf("reviewId" to reviewId)
            )
        }

        binding.rvTimeline.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTimeline.adapter = adapter

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
