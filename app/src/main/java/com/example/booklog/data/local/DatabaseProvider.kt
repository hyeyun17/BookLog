package com.example.booklog.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "booklog.db"
            )
                .addMigrations(DatabaseMigrations.FROM_2_TO_3, DatabaseMigrations.FROM_3_TO_4)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedInitialData(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        seedIfEmpty(db)
                    }
                })
                .build()
                .also { INSTANCE = it }
        }
    }

    private fun seedIfEmpty(db: SupportSQLiteDatabase) {
        try {
            seedInitialData(db)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun seedInitialData(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            val sampleData = listOf(
                arrayOf("seed_sample_1", "9788936434120", "소년이 온다", "한강", "https://image.aladin.co.kr/product/4086/97/cover500/8936434128_2.jpg", "마음이 깊게 아리고 묵직해지는 책. 잊지 말아야 할 역사와 인간의 숭고함에 대해 깊이 생각하게 만든다.", 5.0, 1726000000000L, 37.5759, 126.9768),
                arrayOf("seed_sample_2", "9788936434595", "채식주의자", "한강", "https://image.aladin.co.kr/product/29137/2/cover500/8936434594_2.jpg", "인간의 폭력성과 욕망, 상처에 대한 흡입력 있는 시선. 정교하고 감각적인 문장이 인상적이다.", 4.5, 1725500000000L, 37.5563, 126.9229),
                arrayOf("seed_sample_3", "9788954682152", "작별하지 않는다", "한강", "https://image.aladin.co.kr/product/27877/5/cover500/8954682154_3.jpg", "지극한 사랑이 지닌 깊은 빛과 온기. 눈송이처럼 조용히 마음속에 젖어드는 아련한 이야기.", 5.0, 1725000000000L, 33.4996, 126.5312),
                arrayOf("seed_sample_4", "9791161571188", "불편한 편의점", "김호연", "https://image.aladin.co.kr/product/29045/74/cover500/k192836746_2.jpg", "지친 일상 속 따뜻한 위로와 웃음을 선사해 준 이웃들의 이야기. 청파동 편의점이 따뜻한 안식처가 되었다.", 5.0, 1724500000000L, 37.5451, 126.9654),
                arrayOf("seed_sample_5", "9791124575178", "달러구트 꿈 백화점", "이미예", "https://image.aladin.co.kr/product/39638/93/cover500/k382130171_1.jpg", "잠들어야만 입장할 수 있는 신비로운 백화점. 마음이 부드러워지는 따스한 판타지 소설.", 4.5, 1724000000000L, 37.5112, 127.0590),
                arrayOf("seed_sample_6", "9791189327156", "물고기는 존재하지 않는다", "룰루 밀러", "https://image.aladin.co.kr/product/28465/73/cover500/k092835920_2.jpg", "과학과 삶의 혼돈, 그리고 희망을 다룬 지적이고 아름다운 자서전. 세상을 바라보는 틀을 깨어준다.", 5.0, 1723500000000L, 37.5802, 126.9830),
                arrayOf("seed_sample_7", "9791168473690", "세이노의 가르침", "세이노", "https://image.aladin.co.kr/product/30929/51/cover500/s192030030_1.jpg", "직설적이지만 뼈를 때리는 현실적인 조언들. 매일 조금씩 꺼내 읽으며 나 자신을 되돌아보게 된다.", 4.0, 1723000000000L, 37.4979, 127.0276),
                arrayOf("seed_sample_8", "9791130646381", "이처럼 사소한 것들", "클레어 키건", "https://image.aladin.co.kr/product/32938/68/cover500/k472936042_2.jpg", "짧지만 긴 여운을 남기는 크리스마스의 선물 같은 소설. 용기와 다정함의 진정한 의미를 깨닫게 된다.", 4.5, 1722500000000L, 35.1796, 129.0756),
                arrayOf("seed_sample_9", "9791198363510", "아몬드", "손원평", "https://image.aladin.co.kr/product/31893/32/cover500/k212833749_2.jpg", "감정을 느끼지 못하는 소년의 성장기. 공감과 이해의 의미에 대해 성찰하게 만든다.", 4.5, 1722000000000L, 37.5410, 127.0560),
                arrayOf("seed_sample_10", "9788956269023", "책의 운명", "김겨울", "https://image.aladin.co.kr/product/3126/38/cover500/8956269025_1.jpg", "책과 함께하는 삶에 대한 다채롭고 지적인 기쁨. 읽는 내내 독서의 즐거움을 다시 느끼게 해준다.", 4.0, 1721500000000L, 37.5665, 126.9780)
            )
            for (item in sampleData) {
                val token = item[0] as String
                val isbn13 = item[1] as String
                val title = item[2] as String
                val author = item[3] as String
                val coverUrl = item[4] as String
                val content = item[5] as String
                val rating = item[6] as Double
                val createdAt = item[7] as Long
                val lat = item[8] as Double
                val lng = item[9] as Double

                db.execSQL(
                    "INSERT OR IGNORE INTO review (draftToken, isbn13, title, author, coverUrl, photoUri, content, rating, createdAt, lat, lng) VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?)",
                    arrayOf(token, isbn13, title, author, coverUrl, content, rating, createdAt, lat, lng)
                )
                db.execSQL(
                    "UPDATE review SET coverUrl = ? WHERE draftToken = ?",
                    arrayOf(coverUrl, token)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
