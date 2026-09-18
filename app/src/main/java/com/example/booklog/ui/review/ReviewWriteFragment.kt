package com.example.booklog.ui.review

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.example.booklog.R
import com.example.booklog.data.location.ReviewLocationProvider
import com.example.booklog.databinding.FragmentReviewWriteBinding
import kotlinx.coroutines.launch

class ReviewWriteFragment : Fragment(R.layout.fragment_review_write) {
    private var _binding: FragmentReviewWriteBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val model: ReviewWriteViewModel by viewModels { ReviewWriteViewModel.Factory }
    private var rendering = false
    private var displayedPhoto: String? = null

    private val requestLocation = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        model.permissionResult()
    }
    private val pickPhoto = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                model.setPhoto(uri.toString())
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "사진 접근 권한을 얻지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReviewWriteBinding.bind(view)
        displayedPhoto = null
        // 입력값 복원은 SavedStateHandle 한 곳에서 관리한다.
        listOf(binding.etTitle, binding.etAuthor, binding.etContent, binding.ratingBar).forEach { it.isSaveEnabled = false }
        binding.etTitle.doAfterTextChanged { captureDraft() }
        binding.etAuthor.doAfterTextChanged { captureDraft() }
        binding.etContent.doAfterTextChanged { captureDraft() }
        binding.ratingBar.setOnRatingBarChangeListener { _, _, fromUser -> if (fromUser) captureDraft() }
        binding.btnPickPhoto.setOnClickListener { pickPhoto.launch(arrayOf("image/*")) }
        binding.btnSave.setOnClickListener {
            model.requestSave(ReviewLocationProvider(requireContext()).hasPermission())
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                model.state.collect { state ->
                    render(state)
                    state.error?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        model.clearError()
                    }
                    if (state.requestPermission) {
                        model.permissionLaunched()
                        requestLocation.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                    if (state.missing) {
                        Toast.makeText(requireContext(), "독후감을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else state.completedId?.let { id ->
                        if (findNavController().currentDestination?.id == R.id.reviewWriteFragment) {
                            if (model.isEditing) findNavController().popBackStack()
                            else findNavController().navigate(R.id.reviewDetailFragment, bundleOf("reviewId" to id),
                                navOptions { popUpTo(R.id.reviewWriteFragment) { inclusive = true } })
                        }
                    }
                }
            }
        }
    }

    private fun captureDraft() {
        if (rendering || _binding == null) return
        model.updateDraft(model.state.value.draft.copy(title = binding.etTitle.text.toString(),
            author = binding.etAuthor.text.toString(), content = binding.etContent.text.toString(), rating = binding.ratingBar.rating))
    }

    private fun render(state: WriteState) {
        rendering = true
        setText(binding.etTitle, state.draft.title)
        setText(binding.etAuthor, state.draft.author)
        setText(binding.etContent, state.draft.content)
        binding.ratingBar.rating = state.draft.rating
        val editable = state.ready && !state.saving
        listOf(binding.etTitle, binding.etAuthor, binding.etContent, binding.ratingBar, binding.btnPickPhoto).forEach { it.isEnabled = editable }
        binding.btnSave.isEnabled = !state.loading && !state.saving && state.completedId == null
        binding.ivPickedPhoto.visibility = if (state.draft.photo == null) View.GONE else View.VISIBLE
        if (state.draft.photo != displayedPhoto) {
            displayedPhoto = state.draft.photo
            Glide.with(binding.ivPickedPhoto).load(state.draft.photo?.let(Uri::parse)).into(binding.ivPickedPhoto)
        }
        rendering = false
    }

    private fun setText(input: EditText, text: String) {
        if (input.text.toString() != text) { input.setText(text); input.setSelection(text.length) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
