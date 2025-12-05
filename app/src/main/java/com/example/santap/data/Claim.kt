package com.example.santap.data

data class Claim(
    // ID dokumen
    val id: String = "",
    val foodId: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val foodName: String = "",
    val location: String = "",
    val expiryDate: String = "",
    val expiryTime: String = "",
    // Menggunakan Long untuk timestamp klien
    val createdAt: Long = 0L,
    val verificationCode: String = "",
    val status: String = "PENDING",
    // Timestamp konfirmasi
    val confirmedAt: Long = 0L,
    val claimedPortions: Int = 0
)