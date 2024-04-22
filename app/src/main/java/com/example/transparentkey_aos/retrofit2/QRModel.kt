package com.example.transparentkey_aos.retrofit2

data class QRModel(
    val id: String,
    val date: String
)

data class ResponseQRModel(
    val success: Boolean,
    val message: String
)
