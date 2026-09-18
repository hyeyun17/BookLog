package com.example.booklog.data.aladin

import retrofit2.http.GET
import retrofit2.http.Query

interface AladinApi {

    @GET("ItemSearch.aspx")
    suspend fun searchBooks(
        @Query("ttbkey") ttbKey: String,
        @Query("Query") query: String,

        @Query("QueryType") queryType: String = "Keyword",

        @Query("MaxResults") maxResults: Int = 10,
        @Query("start") start: Int = 1,
        @Query("SearchTarget") target: String = "Book",
        @Query("Cover") cover: String = "Mid",
        @Query("output") output: String = "JS",
        @Query("Version") version: String = "20131101"
    ): AladinSearchResponse

    @GET("ItemList.aspx")
    suspend fun getBestSellers(
        @Query("ttbkey") ttbKey: String,
        @Query("QueryType") queryType: String = "Bestseller",
        @Query("MaxResults") maxResults: Int = 10,
        @Query("start") start: Int = 1,
        @Query("SearchTarget") target: String = "Book",
        @Query("Cover") cover: String = "Mid",
        @Query("output") output: String = "JS",
        @Query("Version") version: String = "20131101"
    ): AladinSearchResponse

    @GET("ItemLookUp.aspx")
    suspend fun lookupBookDetail(
        @Query("ttbkey") ttbKey: String,
        @Query("ItemId") isbn13: String,
        @Query("ItemIdType") type: String = "ISBN13",
        @Query("Cover") cover: String = "Mid",

        @Query("OptResult") opt: String = "ratingInfo,reviewList",

        @Query("output") output: String = "JS",
        @Query("Version") version: String = "20131101"
    ): AladinDetailResponse
}
