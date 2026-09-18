package com.example.booklog.data.local

data class Review(
    val id: Long = 0L,
    val isbn13: String? = null,

    val title: String,
    val author: String,

    val coverUrl: String? = null,

    val photoUri: String? = null,

    val content: String,
    val rating: Float,

    val createdAt: Long,

    val lat: Double? = null,
    val lng: Double? = null,
    val draftToken: String? = null
)
