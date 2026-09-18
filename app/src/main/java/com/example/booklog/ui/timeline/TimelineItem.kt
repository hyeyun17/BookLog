package com.example.booklog.ui.timeline

import com.example.booklog.data.local.Review

sealed class TimelineItem {
    data class MonthHeader(val year: Int, val month: Int) : TimelineItem()
    data class ReviewItem(val review: Review) : TimelineItem()
}
