package com.example.booklog

import com.example.booklog.data.local.Review
import com.example.booklog.data.local.toEntity
import com.example.booklog.data.local.toReview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewMapperTest {
    private val review = Review(
        id = 42L, isbn13 = "0123456789012", title = "제목", author = "저자",
        coverUrl = "https://example.com/cover.jpg", photoUri = "content://photos/42",
        content = "독후감", rating = 4f, createdAt = 1_700_000_000_000L,
        lat = 37.5, lng = 127.0
    )

    @Test fun roundTripPreservesEveryField() {
        assertEquals(review, review.toEntity().toReview())
    }

    @Test fun missingLocationAndPhotoRemainAbsent() {
        val withoutLocation = review.copy(lat = null, lng = null, photoUri = null)
        val restored = withoutLocation.toEntity().toReview()
        assertEquals(withoutLocation, restored)
        assertNull(restored.lat)
        assertNull(restored.lng)
    }

    @Test fun editingPreservesIdentityAndCreationData() {
        val edited = review.copy(title = "수정", content = "수정한 내용", rating = 3f).toEntity().toReview()
        assertEquals(review.id, edited.id)
        assertEquals(review.isbn13, edited.isbn13)
        assertEquals(review.createdAt, edited.createdAt)
        assertEquals(review.lat, edited.lat)
        assertEquals(review.lng, edited.lng)
    }
}
