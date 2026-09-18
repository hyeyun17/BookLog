package com.example.booklog

import android.content.Context
import androidx.room.Room
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.booklog.data.local.AppDatabase
import com.example.booklog.data.local.DatabaseMigrations
import com.example.booklog.data.local.Review
import com.example.booklog.data.repository.ReviewRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ReviewDatabaseJvmTest {
    private val context = RuntimeEnvironment.getApplication()

    @Test fun duplicateDraftRequestsUseOneRowAndUpdatePreservesToken() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repository = ReviewRepository(db.reviewDao())
            val review = Review(title = "book", author = "author", content = "review", rating = 4f, createdAt = 123)
            val first = repository.saveDraft(review, "draft-1")
            val second = repository.saveDraft(review, "draft-1")
            assertEquals(first, second)
            assertEquals(1, repository.getAll().size)
            val saved = requireNotNull(repository.getById(first))
            repository.update(saved.copy(content = "edited"))
            assertEquals(first, repository.getByDraftToken("draft-1")?.id)
            assertEquals("edited", repository.getByDraftToken("draft-1")?.content)
            repository.deleteById(first)
            try { repository.update(saved); fail("Updating a deleted record must fail") }
            catch (_: IllegalStateException) { }
        } finally { db.close() }
    }

    @Test fun repositorySavesUpdatesAndDeletesWithoutChangingIdentity() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repository = ReviewRepository(db.reviewDao())
            val original = Review(
                isbn13 = "0123456789012", title = "제목", author = "저자", content = "내용",
                rating = 4f, createdAt = 123L, photoUri = "content://photos/1"
            )
            val id = repository.addAndReturnId(original)
            val saved = requireNotNull(repository.getById(id))
            assertEquals(original.copy(id = id), saved)
            repository.update(saved.copy(content = "수정"))
            assertEquals(saved.copy(content = "수정"), repository.getById(id))
            assertEquals(1, repository.getAll().size)
            repository.deleteById(id)
            assertNull(repository.getById(id))
            assertTrue(repository.getAll().isEmpty())
        } finally {
            db.close()
        }
    }

    @Test fun migrationPreservesVersionTwoRecordsAndAllowsMissingLocation() = runBlocking {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
            old.execSQL("""
                CREATE TABLE review (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    isbn13 TEXT, title TEXT NOT NULL, author TEXT NOT NULL,
                    coverUrl TEXT, photoUri TEXT, content TEXT NOT NULL,
                    rating REAL NOT NULL, createdAt INTEGER NOT NULL,
                    lat REAL NOT NULL, lng REAL NOT NULL
                )
            """.trimIndent())
            old.execSQL("""
                INSERT INTO review VALUES
                (7, '0123456789012', 'title', 'author', 'cover', 'content://photos/7', 'review', 4, 123, 37.5, 127.0)
            """.trimIndent())
            old.version = 2
        }
        val db = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(DatabaseMigrations.FROM_2_TO_3, DatabaseMigrations.FROM_3_TO_4).build()
        try {
            val repository = ReviewRepository(db.reviewDao())
            val preserved = requireNotNull(repository.getById(7))
            assertEquals("0123456789012", preserved.isbn13)
            assertEquals("content://photos/7", preserved.photoUri)
            assertEquals(123L, preserved.createdAt)
            assertEquals(37.5, requireNotNull(preserved.lat), 0.0)
            assertEquals(127.0, requireNotNull(preserved.lng), 0.0)
            val newId = repository.addAndReturnId(preserved.copy(id = 0, lat = null, lng = null))
            assertTrue(newId > 7)
            assertNull(repository.getById(newId)?.lat)
            assertEquals(2, repository.getAll().size)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}
