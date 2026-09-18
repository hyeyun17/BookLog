package com.example.booklog.data.local

fun ReviewEntity.toReview(): Review = Review(
    id = id,
    isbn13 = isbn13,
    title = title,
    author = author,
    coverUrl = coverUrl,
    photoUri = photoUri,
    content = content,
    rating = rating,
    createdAt = createdAt,
    lat = lat,
    lng = lng,
    draftToken = draftToken
)

fun Review.toEntity(): ReviewEntity = ReviewEntity(
    id = id,
    isbn13 = isbn13,
    title = title,
    author = author,
    coverUrl = coverUrl,
    photoUri = photoUri,
    content = content,
    rating = rating,
    createdAt = createdAt,
    lat = lat,
    lng = lng,
    draftToken = draftToken
)
