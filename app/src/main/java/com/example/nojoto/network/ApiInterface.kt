package com.example.nojoto.network


import com.example.nojoto.pojo.UploadImages
import com.example.nojoto.utils.Constant
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @Multipart
    @POST("/api/beta/content.php?cid=7ec99b415af3e88205250e3514ce0fa7")
    suspend fun updatePhoto(
        @Part image: MultipartBody.Part
    ): Response<UploadImages>



}



