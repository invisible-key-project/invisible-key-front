package com.example.transparentkey_aos.retrofit2

data class QRModel(
    val id: Int,
    val date: Long
)

data class ResponseBody(
    val success: Boolean,
    val message: String
)
