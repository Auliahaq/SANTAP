package com.example.santap.data

// Hapus import Firebase Firestore dan utilitas yang tidak digunakan
// import com.google.firebase.firestore.DocumentId
// import com.google.firebase.firestore.ServerTimestamp
// import java.util.Date

data class Claim(
    // ID dokumen dihapus; Supabase/PostgreSQL akan menggunakan kolom 'id' default INT/UUID.
    // Jika Anda ingin menyimpan ID dari klien, gunakan tipe String biasa.
    val id: String = "",
    val foodId: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val foodName: String = "",
    val location: String = "",
    val expiryDate: String = "",
    val expiryTime: String = "",

    // Mengganti @ServerTimestamp Date? menjadi tipe Long (Timestamp Klien)
    // Atau String (jika Anda ingin PostgREST mengurus timestamp di sisi server)
    val createdAt: Long = 0L, // Menggunakan Long (Timestamp Millis Klien)

    val verificationCode: String = "",
    val status: String = "PENDING",

    // confirmedAt tetap Long (Timestamp Millis Klien/Donor)
    val confirmedAt: Long = 0L,

    val claimedPortions: Int = 0
)