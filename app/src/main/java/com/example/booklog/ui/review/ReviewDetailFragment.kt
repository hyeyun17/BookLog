package com.example.booklog.ui.review

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.booklog.R
import com.example.booklog.data.repository.ReviewRepository
import com.example.booklog.databinding.FragmentReviewDetailBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class ReviewDetailFragment : Fragment(R.layout.fragment_review_detail) {

    private var _binding: FragmentReviewDetailBinding? = null
    private val binding get() = requireNotNull(_binding)
    private var deleteDialog: AlertDialog? = null
    private lateinit var repository: ReviewRepository
    private var loadedReviewId: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReviewDetailBinding.bind(view)
        repository = ReviewRepository(requireContext())

        loadedReviewId = arguments?.getLong("reviewId") ?: -1L

        loadReview()
        setupButtons()
    }

    private fun loadReview() {
        binding.btnEdit.isEnabled = false
        binding.btnDelete.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val review = repository.getById(loadedReviewId)
                if (review == null) {
                    Toast.makeText(requireContext(), "독후감을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    return@launch
                }

                binding.tvTitle.text = review.title
                binding.tvAuthor.text = review.author
                binding.tvContent.text = review.content
                binding.ratingBar.rating = review.rating.toFloat()

                val cover = review.coverUrl?.takeIf { it.isNotBlank() } ?: review.photoUri
                com.example.booklog.ui.utils.ImageUtils.loadCover(binding.ivCover, cover)

                review.photoUri?.let {
                    binding.ivPhoto.visibility = View.VISIBLE
                    Glide.with(binding.ivPhoto).load(Uri.parse(it)).into(binding.ivPhoto)
                }
                binding.btnEdit.isEnabled = true
                binding.btnDelete.isEnabled = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "독후감을 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {

        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                R.id.reviewWriteFragment,
                bundleOf("reviewId" to loadedReviewId)
            )
        }

        binding.btnDelete.setOnClickListener {
            deleteDialog = AlertDialog.Builder(requireContext())
                .setTitle("삭제")
                .setMessage("정말 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    binding.btnDelete.isEnabled = false
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            repository.deleteById(loadedReviewId)
                            findNavController().popBackStack()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "삭제하지 못했습니다", Toast.LENGTH_SHORT).show()
                        } finally {
                            _binding?.btnDelete?.isEnabled = true
                        }
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    override fun onDestroyView() {
        deleteDialog?.dismiss()
        deleteDialog = null
        super.onDestroyView()
        _binding = null
    }
}
