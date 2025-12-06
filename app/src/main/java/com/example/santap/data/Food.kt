package com.example.santap.data

data class Food(
    val id: String = "",
    val donorId: String = "",
    val name: String = "",
    val totalPortions: Int = 0,
    val remaining: Int = 0,
    val location: String = "",
    val expiryDate: String = "",
    val expiryTime: String = "",
    val photoUrl: String = "",
    val expiresAt: Long = 0L,
    val description: String? = null

)