package com.example.booklog.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val FROM_3_TO_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE review ADD COLUMN draftToken TEXT")
            db.execSQL("CREATE UNIQUE INDEX index_review_draftToken ON review (draftToken)")
        }
    }

    // 위치 미수집을 null로 표현하며 기존 독후감과 좌표를 보존한다.
    val FROM_2_TO_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE review_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    isbn13 TEXT, title TEXT NOT NULL, author TEXT NOT NULL,
                    coverUrl TEXT, photoUri TEXT, content TEXT NOT NULL,
                    rating REAL NOT NULL, createdAt INTEGER NOT NULL,
                    lat REAL, lng REAL
                )
            """.trimIndent())
            db.execSQL("""
                INSERT INTO review_new (id, isbn13, title, author, coverUrl, photoUri, content, rating, createdAt, lat, lng)
                SELECT id, isbn13, title, author, coverUrl, photoUri, content, rating, createdAt, lat, lng FROM review
            """.trimIndent())
            db.execSQL("DROP TABLE review")
            db.execSQL("ALTER TABLE review_new RENAME TO review")
        }
    }
}
