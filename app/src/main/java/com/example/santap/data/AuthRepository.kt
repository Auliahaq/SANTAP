package com.example.santap.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // REGISTER
    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: String
    ): Result<Unit> {
        return try {
            // 1. Buat akun auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid

            // 2. Simpan data user ke Firestore
            val user = User(
                id = uid,
                role = role,
                email = email,
                name = name,
                phone = phone
            )

            db.collection("users").document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // LOGIN
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // 1. Login ke Firebase Auth
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid

            // 2. Ambil data user dari Firestore
            val snapshot = db.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
                ?: return Result.failure(Exception("User tidak ditemukan di database"))

            Result.success(user.copy(id = uid))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}