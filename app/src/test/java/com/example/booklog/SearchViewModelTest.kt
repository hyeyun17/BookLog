package com.example.booklog

import androidx.lifecycle.SavedStateHandle
import com.example.booklog.data.aladin.AladinBookItem
import com.example.booklog.data.repository.*
import com.example.booklog.ui.search.SearchViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun staleResultCannotOverwriteNewSearch() = runTest {
        val old = CompletableDeferred<BookPage>()
        val source = object : BookSource {
            override suspend fun search(query: String, page: Int): BookPage = if (query == "old") {
                withContext(NonCancellable) { old.await() }
            } else BookPage(listOf(AladinBookItem(title = "new", isbn13 = "2")), false)
            override suspend fun bestSellers() = emptyList<AladinBookItem>()
        }
        val model = SearchViewModel(SavedStateHandle(), source)
        model.search("old"); runCurrent()
        model.search("new"); runCurrent()
        old.complete(BookPage(listOf(AladinBookItem(title = "old", isbn13 = "1")), false))
        advanceUntilIdle()
        assertEquals("new", model.state.value.books.single().title)
    }

    @Test fun paginationDeduplicatesAndRetriesFailedPage() = runTest {
        val pages = mutableListOf<Int>()
        var fail = true
        val source = object : BookSource {
            override suspend fun search(query: String, page: Int): BookPage {
                pages += page
                if (page == 2 && fail) throw java.io.IOException()
                return if (page == 1) BookPage(listOf(AladinBookItem(isbn13 = "1")), true)
                else BookPage(listOf(AladinBookItem(isbn13 = "1"), AladinBookItem(isbn13 = "2")), false)
            }
            override suspend fun bestSellers() = emptyList<AladinBookItem>()
        }
        val model = SearchViewModel(SavedStateHandle(), source)
        model.search("book"); advanceUntilIdle()
        model.loadMore(); model.loadMore(); advanceUntilIdle()
        assertNotNull(model.state.value.error)
        assertEquals(1, model.state.value.books.size)
        fail = false
        model.retry(); advanceUntilIdle()
        assertEquals(listOf(1, 2, 2), pages)
        assertEquals(listOf("1", "2"), model.state.value.books.map { it.isbn13 })
        assertFalse(model.state.value.hasNext)
    }
}
