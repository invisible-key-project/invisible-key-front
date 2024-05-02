package com.example.transparentkey_aos.retrofit2

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiQRCode {
    @POST("/app/embed/qrdata/")
    fun sendQRData(@Body data: QRModel): Call<ResponseBody>

    @GET("/app/embed/qrdata/")
    fun receiveQRImage(): Call<ResponseBody>
}