package com.example.booklog.data.aladin

import com.example.booklog.BuildConfig
import com.example.booklog.data.repository.BookRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AladinClient {

    val TTB_KEY: String get() = BuildConfig.ALADIN_TTB_KEY

    private const val BASE_URL = "https://www.aladin.co.kr/ttb/api/"

    val repository: BookRepository by lazy { BookRepository(api, { TTB_KEY }) }

    val api: AladinApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AladinApi::class.java)
    }
}
