package com.example.booklog.ui.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.booklog.data.local.Review
import com.example.booklog.data.location.ReviewLocationProvider
import com.example.booklog.data.repository.ReviewRepository
import com.example.booklog.data.repository.ReviewStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ReviewDraft(val title: String = "", val author: String = "", val content: String = "", val rating: Float = 0f, val photo: String? = null)
data class WriteState(
    val draft: ReviewDraft = ReviewDraft(), val loading: Boolean = true, val ready: Boolean = false,
    val saving: Boolean = false, val requestPermission: Boolean = false,
    val completedId: Long? = null, val missing: Boolean = false, val error: String? = null
)
data class ReviewCoordinates(val latitude: Double, val longitude: Double)

class ReviewWriteViewModel(
    private val saved: SavedStateHandle,
    private val repository: ReviewStore,
    private val locate: suspend () -> ReviewCoordinates?
) : ViewModel() {
    private val mutableState = MutableStateFlow(WriteState())
    val state = mutableState.asStateFlow()
    private val reviewId = saved.get<Long>("reviewId") ?: -1L
    val isEditing = reviewId > 0L
    private val token = saved.get<String>("draftToken") ?: UUID.randomUUID().toString().also { saved["draftToken"] = it }
    private var original: Review? = null

    init { load() }

    private fun load() {
        mutableState.value = state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val completed = saved.get<Long>("completedId")
                    ?: if (!isEditing) repository.getByDraftToken(token)?.id else null
                if (completed != null) {
                    mutableState.value = state.value.copy(loading = false, completedId = completed)
                    return@launch
                }
                original = if (isEditing) repository.getById(reviewId) else null
                if (isEditing && original == null) {
                    mutableState.value = state.value.copy(loading = false, missing = true)
                    return@launch
                }
                var draft = if (saved.get<Boolean>("draftInitialized") == true) {
                    ReviewDraft(saved["draftTitle"] ?: "", saved["draftAuthor"] ?: "", saved["draftContent"] ?: "",
                        saved["draftRating"] ?: 0f, saved["draftPhoto"])
                } else original?.let { ReviewDraft(it.title, it.author, it.content, it.rating, it.photoUri) }
                    ?: ReviewDraft(title = saved["title"] ?: "", author = saved["author"] ?: "")
                saved.get<String>("pendingPhoto")?.let { draft = draft.copy(photo = it) }
                saved.remove<String>("pendingPhoto")
                persist(draft)
                mutableState.value = WriteState(draft = draft, loading = false, ready = true)
                if (saved.get<Boolean>("pendingSave") == true) save()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableState.value = state.value.copy(loading = false, error = "독후감을 불러오지 못했습니다. 저장 버튼으로 다시 시도해 주세요")
            }
        }
    }

    fun updateDraft(draft: ReviewDraft) {
        if (!state.value.ready || state.value.saving) return
        persist(draft)
        mutableState.value = state.value.copy(draft = draft)
    }

    fun setPhoto(photo: String) {
        if (state.value.ready) updateDraft(state.value.draft.copy(photo = photo))
        else saved["pendingPhoto"] = photo
    }

    private fun persist(draft: ReviewDraft) {
        saved["draftInitialized"] = true
        saved["draftTitle"] = draft.title
        saved["draftAuthor"] = draft.author
        saved["draftContent"] = draft.content
        saved["draftRating"] = draft.rating
        saved["draftPhoto"] = draft.photo
    }

    fun requestSave(hasLocationPermission: Boolean) {
        if (state.value.loading || state.value.saving || state.value.completedId != null) return
        if (!state.value.ready) { load(); return }
        if (!valid()) return
        if (!isEditing && !hasLocationPermission && saved.get<Boolean>("permissionRequested") != true) {
            saved["permissionRequested"] = true
            mutableState.value = state.value.copy(requestPermission = true)
        } else save()
    }

    fun permissionLaunched() { mutableState.value = state.value.copy(requestPermission = false) }
    fun permissionResult() {
        if (state.value.loading) saved["pendingSave"] = true else save()
    }
    fun clearError() { mutableState.value = state.value.copy(error = null) }

    private fun valid(): Boolean {
        val draft = state.value.draft
        if (draft.title.isBlank() || draft.content.isBlank()) {
            mutableState.value = state.value.copy(error = "제목과 내용을 입력해 주세요")
            return false
        }
        return true
    }

    fun save() {
        if (!state.value.ready || state.value.loading || state.value.saving || state.value.completedId != null || !valid()) return
        val draft = state.value.draft
        mutableState.value = state.value.copy(saving = true, error = null, requestPermission = false)
        saved["pendingSave"] = true
        viewModelScope.launch {
            try {
                val record = original?.copy(title = draft.title.trim(), author = draft.author.trim(), content = draft.content.trim(),
                    rating = draft.rating, photoUri = draft.photo)
                    ?: Review(title = draft.title.trim(), author = draft.author.trim(), content = draft.content.trim(),
                        rating = draft.rating, photoUri = draft.photo, createdAt = System.currentTimeMillis(),
                        isbn13 = saved["isbn13"], coverUrl = saved["coverUrl"])
                val id = if (isEditing) {
                    repository.update(record)
                    reviewId
                } else {
                    val location = locate()
                    // 동일 초안의 재시도는 DB에서도 같은 기록으로 처리한다.
                    repository.saveDraft(record.copy(lat = location?.latitude, lng = location?.longitude), token)
                }
                saved["completedId"] = id
                saved["pendingSave"] = false
                mutableState.value = state.value.copy(saving = false, completedId = id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saved["pendingSave"] = false
                mutableState.value = state.value.copy(saving = false, error = "저장하지 못했습니다. 다시 시도해 주세요")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val context = requireNotNull(this[APPLICATION_KEY]).applicationContext
                val location = ReviewLocationProvider(context)
                ReviewWriteViewModel(createSavedStateHandle(), ReviewRepository(context)) {
                    location.currentLocation()?.let { ReviewCoordinates(it.latitude, it.longitude) }
                }
            }
        }
    }
}
