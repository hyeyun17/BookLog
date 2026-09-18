package com.example.booklog

import androidx.lifecycle.SavedStateHandle
import com.example.booklog.data.local.Review
import com.example.booklog.data.repository.ReviewStore
import com.example.booklog.ui.review.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewWriteViewModelTest {
    private class Store : ReviewStore {
        var record: Review? = null
        var saves = 0
        var updates = 0
        var fail = false
        var loadGate: CompletableDeferred<Unit>? = null
        override suspend fun getById(id: Long) = record?.takeIf { it.id == id }
        override suspend fun getByDraftToken(token: String): Review? {
            loadGate?.await()
            return record?.takeIf { it.draftToken == token }
        }
        override suspend fun saveDraft(review: Review, token: String): Long {
            if (fail) throw java.io.IOException()
            saves++
            if (record?.draftToken != token) record = review.copy(id = 9, draftToken = token)
            return requireNotNull(record).id
        }
        override suspend fun update(review: Review) { updates++; record = review }
    }
    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun draftIncludingPhotoSurvivesSavedStateRestoration() = runTest {
        val saved = SavedStateHandle(mapOf("title" to "book", "isbn13" to "0123456789012"))
        val store = Store()
        val model = ReviewWriteViewModel(saved, store) { null }
        advanceUntilIdle()
        val draft = ReviewDraft("changed", "author", "draft text", 4f, "content://photo/1")
        model.updateDraft(draft)
        val restoredHandle = SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })
        val restored = ReviewWriteViewModel(restoredHandle, store) { null }
        advanceUntilIdle()
        assertEquals(draft, restored.state.value.draft)
    }

    @Test fun repeatedClicksAndRecreationProduceOneRecord() = runTest {
        val saved = SavedStateHandle(mapOf("draftToken" to "stable-token"))
        val store = Store()
        val location = CompletableDeferred<ReviewCoordinates?>()
        val model = ReviewWriteViewModel(saved, store) { location.await() }
        advanceUntilIdle()
        model.updateDraft(ReviewDraft(title = "book", content = "review"))
        model.save(); model.save(); runCurrent()
        assertTrue(model.state.value.saving)
        location.complete(null); advanceUntilIdle()
        assertEquals(1, store.saves)
        assertEquals(9L, model.state.value.completedId)
        val restored = ReviewWriteViewModel(SavedStateHandle(mapOf("draftToken" to "stable-token")), store) { null }
        advanceUntilIdle()
        assertEquals(9L, restored.state.value.completedId)
        assertEquals(1, store.saves)
    }

    @Test fun missingLocationAndFailedSaveDoNotLoseDraft() = runTest {
        val store = Store()
        val model = ReviewWriteViewModel(SavedStateHandle(), store) { null }
        advanceUntilIdle()
        model.updateDraft(ReviewDraft(title = "book", content = "review", rating = 5f))
        store.fail = true
        model.save(); advanceUntilIdle()
        assertNotNull(model.state.value.error)
        assertFalse(model.state.value.saving)
        assertEquals("review", model.state.value.draft.content)
        store.fail = false
        model.save(); advanceUntilIdle()
        assertEquals(9L, model.state.value.completedId)
        assertNull(store.record?.lat)
    }

    @Test fun editingKeepsOriginalMetadataAndDoesNotRequestLocation() = runTest {
        val store = Store()
        store.record = Review(id = 4, title = "book", author = "author", content = "old", rating = 3f,
            createdAt = 100L, lat = 37.0, lng = 127.0, isbn13 = "0123456789012", draftToken = "old-token")
        val model = ReviewWriteViewModel(SavedStateHandle(mapOf("reviewId" to 4L)), store) { error("Must not locate for editing") }
        advanceUntilIdle()
        model.updateDraft(model.state.value.draft.copy(content = "edited"))
        model.requestSave(false); advanceUntilIdle()
        assertEquals(1, store.updates)
        assertEquals(100L, store.record?.createdAt)
        assertEquals("old-token", store.record?.draftToken)
        assertEquals("edited", store.record?.content)
    }

    @Test fun permissionDenialStillAllowsSavingAndBlankInputDoesNot() = runTest {
        val store = Store()
        val model = ReviewWriteViewModel(SavedStateHandle(), store) { null }
        advanceUntilIdle()
        model.requestSave(false)
        assertNotNull(model.state.value.error)
        assertFalse(model.state.value.requestPermission)
        model.updateDraft(ReviewDraft(title = "book", content = "review"))
        model.requestSave(false)
        assertTrue(model.state.value.requestPermission)
        model.permissionLaunched()
        model.save(); advanceUntilIdle()
        assertEquals(1, store.saves)
    }

    @Test fun activityResultsReceivedDuringRestorationAreNotLost() = runTest {
        val store = Store()
        val gate = CompletableDeferred<Unit>()
        store.loadGate = gate
        val saved = SavedStateHandle(mapOf("draftInitialized" to true, "draftTitle" to "book", "draftContent" to "review"))
        val model = ReviewWriteViewModel(saved, store) { null }
        runCurrent()
        model.setPhoto("content://photo/restored")
        model.permissionResult()
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals("content://photo/restored", store.record?.photoUri)
        assertEquals(1, store.saves)
    }
}
