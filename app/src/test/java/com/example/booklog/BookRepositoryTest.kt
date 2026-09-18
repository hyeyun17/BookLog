package com.example.booklog

import com.example.booklog.data.aladin.*
import com.example.booklog.data.repository.*
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class BookRepositoryTest {
    private class FakeApi : AladinApi {
        var calls = 0
        var lastPage = 0
        var response = AladinSearchResponse(item = List(20) { AladinBookItem(isbn13 = "$it") }, totalResults = 21)
        var failure: Exception? = null
        override suspend fun searchBooks(ttbKey: String, query: String, queryType: String, maxResults: Int,
            start: Int, target: String, cover: String, output: String, version: String): AladinSearchResponse {
            calls++; lastPage = start
            failure?.let { throw it }
            return response
        }
        override suspend fun getBestSellers(ttbKey: String, queryType: String, maxResults: Int, start: Int,
            target: String, cover: String, output: String, version: String) = response
        override suspend fun lookupBookDetail(ttbKey: String, isbn13: String, type: String, cover: String,
            opt: String, output: String, version: String) = AladinDetailResponse()
    }

    @Test fun cacheReusesNormalizedQueryAndExpires() = runTest {
        val api = FakeApi()
        var clock = 0L
        val repository = BookRepository(api, { "test-key" }, { clock })
        assertTrue(repository.search(" book ", 1).hasNext)
        repository.search("book", 1)
        assertEquals(1, api.calls)
        clock = BookRepository.CACHE_TTL
        repository.search("book", 1)
        assertEquals(2, api.calls)
        api.response = AladinSearchResponse(item = listOf(AladinBookItem(isbn13 = "last")), totalResults = 21)
        assertFalse(repository.search("book", 2).hasNext)
        assertEquals(2, api.lastPage)
    }

    @Test fun apiErrorIsNotCachedAsEmptyResults() = runTest {
        val api = FakeApi()
        api.response = Gson().fromJson("""{"errorCode":8,"errorMessage":"Invalid key"}""", AladinSearchResponse::class.java)
        val repository = BookRepository(api, { "test-key" })
        try { repository.search("book", 1); fail("Expected API failure") }
        catch (e: BookServiceException) { assertEquals(BookServiceException.Reason.API_ERROR, e.reason) }
        api.response = AladinSearchResponse(item = emptyList(), totalResults = 0)
        assertTrue(repository.search("book", 1).books.isEmpty())
        assertEquals(2, api.calls)
    }

    @Test fun missingItemsAndExplicitNullFieldsAreHandled() = runTest {
        val api = FakeApi()
        api.response = AladinSearchResponse()
        val repository = BookRepository(api, { "test-key" })
        try { repository.search("book", 1); fail("Expected malformed response") }
        catch (e: BookServiceException) { assertEquals(BookServiceException.Reason.INVALID_RESPONSE, e.reason) }
        api.response = Gson().fromJson("""{"item":[{"title":null,"author":null,"cover":null,"isbn13":null}],"totalResults":1}""", AladinSearchResponse::class.java)
        val book = repository.search("book", 1).books.single()
        assertEquals("", book.title)
        assertEquals("", book.isbn13)
    }

    @Test fun networkFailureCanBeRetriedAndMissingKeyDoesNotCallApi() = runTest {
        val api = FakeApi()
        try { BookRepository(api, { "" }).search("book", 1); fail("Expected missing key") }
        catch (e: BookServiceException) { assertEquals(BookServiceException.Reason.MISSING_KEY, e.reason) }
        assertEquals(0, api.calls)
        val repository = BookRepository(api, { "test-key" })
        api.failure = IOException()
        try { repository.search("book", 1); fail("Expected network failure") } catch (_: IOException) { }
        api.failure = null
        repository.search("book", 1)
        assertEquals(2, api.calls)
    }
}
