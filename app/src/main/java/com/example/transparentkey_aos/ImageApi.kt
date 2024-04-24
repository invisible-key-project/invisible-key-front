package com.example.transparentkey_aos

import com.example.transparentkey_aosdata.ServerResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageApi {
    @Multipart
    @POST("/upload/image/")
    fun sendImage(@Part watermark: MultipartBody.Part): Call<ServerResponse>

}