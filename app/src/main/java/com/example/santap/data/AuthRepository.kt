package com.example.santap.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Membuat akun Auth dan menyimpan profil User.
     */
    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: String
    ): Result<Unit> {
        return try {
            // Buat akun Auth dan dapatkan UID.
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid

            val user = User(
                id = uid,
                role = role,
                email = email,
                name = name,
                phone = phone
            )

            // Simpan data user ke Firestore.
            db.collection("users").document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login dan ambil data User.
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Login ke Firebase Auth.
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid

            // Ambil data User dari Firestore.
            val snapshot = db.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
                ?: return Result.failure(Exception("User tidak ditemukan di database"))

            Result.success(user.copy(id = uid))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Log out pengguna.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Dapatkan UID pengguna.
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
}