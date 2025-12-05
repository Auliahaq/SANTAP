package com.example.santap.data

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.random.Random

// Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage

class FoodRepository(
    private val supabase: SupabaseClient = SupabaseProvider.client,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    companion object {
        private const val BUCKET_NAME = "santap-photos"          // samakan dengan nama bucket di Supabase
        private const val FOOD_PATH_PREFIX = "images/food_postings"
    }

    // -------------------------
    // 1. Tambah Donasi (Donor)
    // -------------------------
    suspend fun addFoodDonation(
        context: Context,
        name: String,
        totalPortions: Int,
        expiryDate: String,
        expiryTime: String,
        expiresAt: Long,
        location: String,
        imageUri: Uri?
    ): Result<Unit> {
        return try {
            val donorId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User belum login"))

            if (imageUri == null) {
                return Result.failure(Exception("Foto makanan wajib diunggah."))
            }

            val docRef = db.collection("foods").document()
            val foodId = docRef.id

            // 1. Upload foto ke Supabase Storage
            val photoUrl = uploadFoodImage(context, imageUri, foodId).getOrElse {
                return Result.failure(it)
            }

            val food = Food(
                id = foodId,
                donorId = donorId,
                name = name,
                totalPortions = totalPortions,
                remaining = totalPortions,
                location = location,
                expiryDate = expiryDate,
                expiryTime = expiryTime,
                expiresAt = expiresAt,
                photoUrl = photoUrl
            )

            docRef.set(food).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------
    // Helper: Upload Foto ke Supabase Storage
    // -------------------------
    suspend fun uploadFoodImage(
        context: Context,
        imageUri: Uri,
        foodId: String
    ): Result<String> {
        return try {
            val filePath = "$FOOD_PATH_PREFIX/$foodId.jpg"

            // 1. Baca file dari Uri → ByteArray
            val imageBytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    input.readBytes()
                } ?: throw Exception("Gagal membaca file foto.")
            }

            // 2. Ambil bucket dari Supabase Storage
            val bucket = supabase.storage.from(BUCKET_NAME)

            // 3. Upload (suspend, tidak perlu .await())
            withContext(Dispatchers.IO) {
                bucket.upload(
                    path = filePath,
                    data = imageBytes,
                    upsert = true
                )
            }

            // 4. Ambil public URL (pastikan bucket public di dashboard Supabase)
            val downloadUrl = bucket.publicUrl(filePath)

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------
// 2. List Donasi untuk Donor
// -------------------------
    suspend fun getFoodsForDonor(): Result<List<Food>> {
        return try {
            val donorId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User belum login"))

            val now = System.currentTimeMillis()

            val snapshot = db.collection("foods")
                .whereEqualTo("donorId", donorId)
                .get()
                .await()

            val foods = snapshot.documents
                .mapNotNull { it.toObject(Food::class.java) }
                // Hanya tampilkan yang masih ada porsinya dan belum lewat batas waktu
                .filter { it.remaining > 0 && it.expiresAt > now }
                .sortedByDescending { it.expiresAt }

            Result.success(foods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------
// 3. List Donasi untuk Penerima
// -------------------------
    suspend fun getFoodsForReceiver(): Result<List<Food>> {
        return try {
            val now = System.currentTimeMillis()

            val snapshot = db.collection("foods")
                // Sudah filter yang belum lewat batas waktu
                .whereGreaterThan("expiresAt", now)
                .get()
                .await()

            val foods = snapshot.documents
                .mapNotNull { it.toObject(Food::class.java) }
                // Tambahan: hanya yang masih punya porsi
                .filter { it.remaining > 0 }
                .sortedByDescending { it.expiresAt }

            Result.success(foods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------
    // Helper: kode verifikasi acak 6 digit
    // -------------------------
    private fun generateVerificationCode(): String =
        Random.nextInt(100000, 999999).toString()

    // ----------------------------------------------------
    // 4. Receiver klik "Ambil Makanan" → PROSES KLAIM
    // ----------------------------------------------------
    suspend fun processFoodClaim(
        food: Food,
        claimedPortions: Int,
        receiverName: String?
    ): Result<String> {
        val receiverId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User belum login"))

        val foodRef = db.collection("foods").document(food.id)

        return try {
            val code = "SANTAP-${generateVerificationCode()}"

            db.runTransaction { transaction ->
                val foodSnap = transaction.get(foodRef)
                val currentRemaining = foodSnap.getLong("remaining")?.toInt() ?: 0

                if (claimedPortions <= 0) throw Exception("Jumlah porsi tidak valid.")
                if (claimedPortions > currentRemaining) throw Exception("Porsi tidak mencukupi.")

                val newRemaining = currentRemaining - claimedPortions
                transaction.update(foodRef, "remaining", newRemaining)

                val claimRef = db.collection("claims").document()
                val newClaim = Claim(
                    id = claimRef.id,
                    foodId = food.id,
                    receiverId = receiverId,
                    receiverName = receiverName.orEmpty(),
                    foodName = food.name,
                    location = food.location,
                    expiryDate = food.expiryDate,
                    expiryTime = food.expiryTime,
                    verificationCode = code,
                    status = "PENDING",
                    claimedPortions = claimedPortions
                )

                transaction.set(claimRef, newClaim)
                code
            }.await().let { Result.success(it) }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // 5. Donor mencari klaim berdasarkan kode verifikasi
    // ----------------------------------------------------
    suspend fun getClaimByCode(code: String): Result<Pair<Claim, Food>> {
        return try {
            val snapshot = db.collection("claims")
                .whereEqualTo("verificationCode", code)
                .whereEqualTo("status", "PENDING")
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Kode tidak ditemukan atau sudah digunakan."))
            }

            val claimDoc = snapshot.documents.first()
            val claim = claimDoc.toObject(Claim::class.java)
                ?: return Result.failure(Exception("Data klaim tidak valid."))

            val foodDoc = db.collection("foods")
                .document(claim.foodId)
                .get()
                .await()

            val food = foodDoc.toObject(Food::class.java)
                ?: return Result.failure(Exception("Data makanan tidak ditemukan."))

            Result.success(claim to food)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // 6. Donor menekan "Sudah Diambil"
    // ----------------------------------------------------
    suspend fun confirmClaimByCode(code: String): Result<Unit> {
        return try {
            val claimsSnap = db.collection("claims")
                .whereEqualTo("verificationCode", code)
                .whereEqualTo("status", "PENDING")
                .limit(1)
                .get()
                .await()

            if (claimsSnap.isEmpty) {
                return Result.failure(Exception("Kode tidak ditemukan atau sudah dikonfirmasi."))
            }

            val claimDoc = claimsSnap.documents.first()
            val claimRef = claimDoc.reference

            db.runTransaction { tx ->
                val freshClaimSnap = tx.get(claimRef)
                val freshClaimStatus = freshClaimSnap.getString("status") ?: "PENDING"

                if (freshClaimStatus != "PENDING") {
                    throw Exception("Klaim sudah diproses.")
                }

                tx.update(
                    claimRef,
                    mapOf(
                        "status" to "COMPLETED",
                        "confirmedAt" to System.currentTimeMillis()
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------
    // 7. History Penerima
    // -------------------------
    suspend fun getHistoryForReceiver(): Result<List<Claim>> {
        return try {
            val receiverId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User belum login"))

            val snapshot = db.collection("claims")
                .whereEqualTo("receiverId", receiverId)
                .get()
                .await()

            val claims = snapshot.documents
                .mapNotNull { it.toObject(Claim::class.java) }
                .sortedByDescending { it.createdAt }

            Result.success(claims)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------
// 8. History Donor
// -------------------------
    suspend fun getDonorHistory(): Result<List<Food>> {
        return try {
            val donorId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User belum login"))

            val now = System.currentTimeMillis()

            val snapshot = db.collection("foods")
                .whereEqualTo("donorId", donorId)
                .get()
                .await()

            val foods = snapshot.documents
                .mapNotNull { it.toObject(Food::class.java) }
                // MASUK RIWAYAT JIKA:
                // - sudah lewat batas waktu ATAU
                // - porsinya sudah habis (remaining <= 0)
                .filter { food ->
                    food.expiresAt <= now || food.remaining <= 0
                }
                .sortedByDescending { it.expiresAt }

            Result.success(foods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
