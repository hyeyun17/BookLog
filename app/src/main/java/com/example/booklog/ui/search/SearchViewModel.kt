package com.example.booklog.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.booklog.data.aladin.AladinBookItem
import com.example.booklog.data.aladin.AladinClient
import com.example.booklog.data.repository.BookSource
import com.example.booklog.data.repository.bookErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "", val books: List<AladinBookItem> = emptyList(),
    val loading: Boolean = false, val hasNext: Boolean = false, val error: String? = null
)

class SearchViewModel(private val saved: SavedStateHandle, private val source: BookSource) : ViewModel() {
    private val mutableState = MutableStateFlow(SearchState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null
    private var page = 0
    private var generation = 0
    var queryInput: String
        get() = saved["queryInput"] ?: ""
        set(value) { saved["queryInput"] = value }

    init { saved.get<String>("searchQuery")?.takeIf { it.isNotBlank() }?.let(::search) }

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        job?.cancel()
        generation++
        page = 0
        saved["searchQuery"] = trimmed
        mutableState.value = SearchState(query = trimmed)
        loadPage()
    }

    fun loadMore() {
        if (!state.value.loading && state.value.hasNext && state.value.error == null) loadPage()
    }

    fun retry() {
        if (!state.value.loading && state.value.error != null) loadPage()
    }

    fun clearError() { mutableState.value = state.value.copy(error = null) }

    private fun loadPage() {
        val query = state.value.query
        if (query.isBlank()) return
        val requestGeneration = generation
        val nextPage = page + 1
        mutableState.value = state.value.copy(loading = true, error = null)
        job = viewModelScope.launch {
            try {
                val result = source.search(query, nextPage)
                if (requestGeneration != generation) return@launch
                page = nextPage
                val merged = (state.value.books + result.books).distinctBy {
                    it.isbn13.ifBlank { "${it.title}|${it.author}|${it.cover}" }
                }
                mutableState.value = state.value.copy(books = merged, loading = false,
                    hasNext = result.hasNext && merged.size > state.value.books.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestGeneration == generation) {
                    mutableState.value = state.value.copy(loading = false, error = e.bookErrorMessage())
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory { initializer { SearchViewModel(createSavedStateHandle(), AladinClient.repository) } }
    }
}
