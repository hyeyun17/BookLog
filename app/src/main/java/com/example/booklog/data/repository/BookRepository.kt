package com.example.booklog.data.repository

import com.example.booklog.data.aladin.AladinApi
import com.example.booklog.data.aladin.AladinBookItem
import com.example.booklog.data.aladin.AladinSearchResponse
import com.google.gson.JsonParseException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

data class BookPage(val books: List<AladinBookItem>, val hasNext: Boolean)

interface BookSource {
    suspend fun search(query: String, page: Int): BookPage
    suspend fun bestSellers(): List<AladinBookItem>
}

class BookServiceException(val reason: Reason) : Exception(reason.name) {
    enum class Reason { MISSING_KEY, API_ERROR, INVALID_RESPONSE }
}

fun Throwable.bookErrorMessage(): String = when (this) {
    is BookServiceException -> when (reason) {
        BookServiceException.Reason.MISSING_KEY -> "도서 API 키가 설정되지 않았습니다"
        BookServiceException.Reason.API_ERROR -> "도서 서비스에서 요청을 처리하지 못했습니다"
        BookServiceException.Reason.INVALID_RESPONSE -> "도서 응답을 읽지 못했습니다"
    }
    is HttpException -> if (code() == 429) "요청이 많습니다. 잠시 후 다시 시도해 주세요" else "도서 서비스에 연결하지 못했습니다"
    is IOException -> "네트워크 연결을 확인해 주세요"
    is JsonParseException -> "도서 응답을 읽지 못했습니다"
    else -> "도서를 불러오지 못했습니다. 다시 시도해 주세요"
}

class BookRepository(
    private val api: AladinApi,
    private val apiKey: () -> String,
    private val now: () -> Long = { System.nanoTime() / 1_000_000L }
) : BookSource {
    private data class Entry(val page: BookPage, val expiresAt: Long)
    private val cache = LinkedHashMap<String, Entry>(32, 0.75f, true)
    private val mutex = Mutex()

    override suspend fun search(query: String, page: Int): BookPage {
        require(query.isNotBlank() && page in 1..200)
        val normalized = query.trim()
        return cached("search:$page:$normalized", page, PAGE_SIZE) {
            api.searchBooks(ttbKey = key(), query = normalized, start = page, maxResults = PAGE_SIZE)
        }
    }

    override suspend fun bestSellers(): List<AladinBookItem> = cached("bestsellers", 1, 10) {
        api.getBestSellers(ttbKey = key(), maxResults = 10)
    }.books

    private fun key(): String = apiKey().takeIf { it.isNotBlank() }
        ?: throw BookServiceException(BookServiceException.Reason.MISSING_KEY)

    private suspend fun cached(
        cacheKey: String, page: Int, size: Int, fetch: suspend () -> AladinSearchResponse
    ): BookPage = mutex.withLock {
        cache[cacheKey]?.takeIf { it.expiresAt > now() }?.let { return@withLock it.page }
        val response = fetch()
        if (!response.errorCode.isNullOrBlank() && response.errorCode != "0") {
            throw BookServiceException(BookServiceException.Reason.API_ERROR)
        }
        if (!response.errorMessage.isNullOrBlank()) {
            throw BookServiceException(BookServiceException.Reason.API_ERROR)
        }
        val items = response.item ?: if (response.totalResults == 0) emptyList() else {
            throw BookServiceException(BookServiceException.Reason.INVALID_RESPONSE)
        }
        val books = items.filterNotNull().map { book ->
            book.copy(title = book.title.orEmpty(), author = book.author.orEmpty(),
                cover = book.cover.orEmpty(), isbn13 = book.isbn13.orEmpty())
        }
        val hasNext = books.size == size && page < 200 &&
            (response.totalResults?.let { page * size < it } ?: true)
        val result = BookPage(books, hasNext)
        // 캐시 크기와 유효 시간을 제한해 오래된 검색 결과가 쌓이지 않게 한다.
        cache[cacheKey] = Entry(result, now() + CACHE_TTL)
        while (cache.size > 30) cache.remove(cache.keys.first())
        result
    }

    companion object {
        const val PAGE_SIZE = 20
        const val CACHE_TTL = 5 * 60 * 1000L
    }
}
