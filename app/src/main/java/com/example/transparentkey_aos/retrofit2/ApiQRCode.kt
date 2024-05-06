package com.example.transparentkey_aos.retrofit2

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiQRCode {
    @POST("embed/qr_data/")
    fun sendQRData(@Body data: QRModel): Call<ResponseBody>

    @Multipart
    @POST("embed/watermark_img/")
    fun uploadImages(
        @Part image1: MultipartBody.Part,
        @Part image2: MultipartBody.Part
    ): Call<ResponseBody>
}