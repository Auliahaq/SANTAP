package com.example.santap.data
data class Food(
    val id: String = "",
    val donorId: String = "",
    val name: String = "",
    val totalPortions: Int = 0,
    val remaining: Int = 0,
    val location: String = "",
    val expiryDate: String = "",       // teks tanggal, buat ditampilkan
    val expiryTime: String = "",       // teks rentang waktu, buat ditampilkan
    val photoUrl: String = "",
    val expiresAt: Long = 0L           // **timestamp batas pengambilan**
)