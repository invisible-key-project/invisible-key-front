package com.example.transparentkey_aos.retrofit2

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiQRCode {
    @POST("/app/gen_qr/")
    fun sendQRData(@Body data: QRModel): Call<ResponseQRModel>
}