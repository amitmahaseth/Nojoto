package com.example.nojoto.pojo

data class UploadImages(
    val cid: String,
    val description: String,
    val error: Boolean,
    val message: String,
    val result: List<Any>,
    val statusCode: Int,
    val success: Boolean
)