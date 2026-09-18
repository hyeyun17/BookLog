package com.example.booklog.ui.library

import com.example.booklog.data.local.Review

sealed class LibraryItem {
    data class MonthHeader(val year: Int, val month: Int) : LibraryItem()
    data class BookItem(val review: Review) : LibraryItem()
}
