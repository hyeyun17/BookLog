package com.example.booklog.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Query("SELECT * FROM review ORDER BY createdAt DESC")
    suspend fun getAll(): List<ReviewEntity>

    @Query("SELECT * FROM review ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM review WHERE id = :id")
    suspend fun getById(id: Long): ReviewEntity?

    @Query("SELECT COUNT(*) FROM review")
    suspend fun count(): Int

    @Insert
    suspend fun insert(review: ReviewEntity): Long

    @Update
    suspend fun update(review: ReviewEntity): Int

    @Query("SELECT * FROM review WHERE draftToken = :token LIMIT 1")
    suspend fun getByDraftToken(token: String): ReviewEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDraft(review: ReviewEntity): Long

    // 재생성된 작성 화면이 같은 저장 요청을 보내도 기록은 하나만 만든다.
    @Transaction
    suspend fun saveDraft(review: ReviewEntity, token: String): Long {
        val inserted = insertDraft(review.copy(id = 0L, draftToken = token))
        return if (inserted != -1L) inserted else requireNotNull(getByDraftToken(token)).id
    }

    @Query("DELETE FROM review WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun delete(review: ReviewEntity)
}
