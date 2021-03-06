package com.molchanov.cats.network.networkmodels

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody

@JsonClass(generateAdapter = true)
data class PostFavorite constructor(
    @Json(name = "image_id") val imageId: String,
    @Json(name = "sub_id") val username: String
)

@JsonClass(generateAdapter = true)
data class PostUploaded constructor(
    @Json(name = "file") val file: MultipartBody.Part,
    @Json(name = "sub_id") val username: String
)

@JsonClass(generateAdapter = true)
data class PostVote constructor(
    @Json(name = "image_id") val imageId: String,
    @Json(name = "value") val value: Int,
    @Json(name = "sub_id") val username: String
)