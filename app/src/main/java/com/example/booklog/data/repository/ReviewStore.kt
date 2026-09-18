package com.example.booklog.data.repository

import com.example.booklog.data.local.Review

interface ReviewStore {
    suspend fun getById(id: Long): Review?
    suspend fun getByDraftToken(token: String): Review?
    suspend fun saveDraft(review: Review, token: String): Long
    suspend fun update(review: Review)
}
