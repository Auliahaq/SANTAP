package com.example.santap.data

// Hapus import @DocumentId jika Anda memilih solusi ini
// import com.google.firebase.firestore.DocumentId

data class User(
    val id: String = "",
    val role: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val profileLocation: String = "Belum Ditetapkan"
)