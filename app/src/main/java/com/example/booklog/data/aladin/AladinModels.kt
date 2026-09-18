package com.example.booklog.data.aladin

import com.google.gson.annotations.SerializedName

data class AladinSearchResponse(
    @SerializedName("item")
    val item: List<AladinBookItem>? = null,
    @SerializedName("totalResults") val totalResults: Int? = null,
    @SerializedName(value = "errorCode", alternate = ["ErrorCode"]) val errorCode: String? = null,
    @SerializedName(value = "errorMessage", alternate = ["ErrorMessage"]) val errorMessage: String? = null
)

data class AladinBookItem(
    @SerializedName("title")
    val title: String = "",

    @SerializedName("author")
    val author: String = "",

    @SerializedName("cover")
    val cover: String = "",

    @SerializedName("isbn13")
    val isbn13: String = "",

    @SerializedName("description")
    val description: String? = null
)

data class AladinDetailResponse(
    @SerializedName("item")
    val item: List<AladinDetailItem> = emptyList()
)

data class AladinDetailItem(
    @SerializedName("title")
    val title: String = "",

    @SerializedName("author")
    val author: String = "",

    @SerializedName("cover")
    val cover: String = "",

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("isbn13")
    val isbn13: String = "",

    @SerializedName("customerReviewRank")
    val customerReviewRank: Int? = null,

    @SerializedName("bestSellerRank")
    val bestSellerRank: Int? = null,

    @SerializedName("subInfo")
    val subInfo: SubInfo? = null
)

data class SubInfo(
    @SerializedName("ratingInfo")
    val ratingInfo: RatingInfo? = null
)

data class RatingInfo(

    @SerializedName("ratingScore")
    val ratingScore: Float? = null,

    @SerializedName("ratingCount")
    val ratingCount: Int? = null,

    @SerializedName("commentReviewCount")
    val commentReviewCount: Int? = null,

    @SerializedName("myReviewCount")
    val myReviewCount: Int? = null
)
