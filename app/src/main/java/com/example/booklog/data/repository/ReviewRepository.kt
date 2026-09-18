package com.example.booklog.data.repository

import android.content.Context
import kotlinx.coroutines.flow.map
import com.example.booklog.data.local.DatabaseProvider
import com.example.booklog.data.local.Review
import com.example.booklog.data.local.ReviewDao
import com.example.booklog.data.local.toEntity
import com.example.booklog.data.local.toReview

class ReviewRepository(private val dao: ReviewDao) : ReviewStore {

    constructor(context: Context) : this(
        DatabaseProvider.get(context).reviewDao()
    )

    fun observeAll() = dao.observeAll().map { reviews -> reviews.map { it.toReview() } }

    suspend fun getAll(): List<Review> =
        dao.getAll().map { it.toReview() }

    override suspend fun getById(id: Long): Review? =
        dao.getById(id)?.toReview()

    suspend fun add(review: Review) {
        dao.insert(review.toEntity())
    }

    suspend fun addAndReturnId(review: Review): Long {
        return dao.insert(review.toEntity())
    }

    override suspend fun update(review: Review) {
        check(dao.update(review.toEntity()) == 1) { "독후감을 찾을 수 없습니다" }
    }

    override suspend fun getByDraftToken(token: String): Review? = dao.getByDraftToken(token)?.toReview()

    override suspend fun saveDraft(review: Review, token: String): Long = dao.saveDraft(review.toEntity(), token)

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}
