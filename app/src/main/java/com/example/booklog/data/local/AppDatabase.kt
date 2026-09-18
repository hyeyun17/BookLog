package com.example.booklog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ReviewEntity::class],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao
}
