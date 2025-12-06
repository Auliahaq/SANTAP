package com.example.santap.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.santap.data.Claim
import com.example.santap.data.Food
import com.example.santap.data.FoodRepository
import kotlinx.coroutines.launch

class FoodViewModel(
    private val repository: FoodRepository = FoodRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    // data donor
    var donorFoods by mutableStateOf<List<Food>>(emptyList())
        private set

    var donorHistory by mutableStateOf<List<Food>>(emptyList())
        private set

    // data penerima
    var receiverFoods by mutableStateOf<List<Food>>(emptyList())
        private set

    var receiverHistory by mutableStateOf<List<Claim>>(emptyList())
        private set

    // detail dan input
    var selectedFood by mutableStateOf<Food?>(null)
        private set
    var claimedPortionsInput by mutableStateOf(0)
        private set
    var lastVerificationCode by mutableStateOf<String?>(null)
        private set

    // untuk verifikasi oleh donor
    var currentClaim by mutableStateOf<Claim?>(null)
        private set
    var currentClaimFood by mutableStateOf<Food?>(null)
        private set

    // item riwayat yang dipilih
    var historyClaim by mutableStateOf<Claim?>(null)
        private set

    // ========= TAMBAH DONASI OLEH DONOR =========
    fun addDonation(
        context: Context,
        name: String,
        totalPortions: Int,
        expiryDate: String,
        expiryTime: String,
        location: String,
        description: String?,          // ⬅️ deskripsi baru
        imageUri: Uri?,
        onSuccessNavigate: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            // gabungkan tanggal + jam jadi millis
            val expiresAt = mergeDateTime(expiryDate, expiryTime)
            val now = System.currentTimeMillis()
            if (expiresAt <= now) {
                isLoading = false
                errorMessage = "Batas pengambilan tidak boleh di waktu yang lewat."
                return@launch
            }

            val result = repository.addFoodDonation(
                context = context,
                name = name,
                totalPortions = totalPortions,
                expiryDate = expiryDate,
                expiryTime = expiryTime,
                expiresAt = expiresAt,
                location = location,
                description = description,    // ⬅️ ikut dikirim ke repo
                imageUri = imageUri
            )

            isLoading = false

            result.onSuccess {
                successMessage = "Donasi berhasil diposting"
                loadDonorFoods()
                onSuccessNavigate()
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Gagal menyimpan donasi"
            }
        }
    }

    // ========= LIST & RIWAYAT DONOR =========
    fun loadDonorFoods() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.getFoodsForDonor()
            isLoading = false

            result.onSuccess { list ->
                val now = System.currentTimeMillis()
                donorFoods = list.filter { it.expiresAt > now }
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Gagal mengambil data"
            }
        }
    }

    fun loadDonorHistory() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.getDonorHistory()
            isLoading = false

            result.onSuccess { donorHistory = it }
                .onFailure { errorMessage = it.localizedMessage }
        }
    }

    // ========= LIST & RIWAYAT PENERIMA =========
    fun loadReceiverFoods() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.getFoodsForReceiver()
            isLoading = false

            result.onSuccess { list ->
                val now = System.currentTimeMillis()
                receiverFoods = list.filter { it.expiresAt > now }
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Gagal mengambil data"
            }
        }
    }

    fun loadReceiverHistory() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.getHistoryForReceiver()
            isLoading = false

            result.onSuccess { receiverHistory = it }
                .onFailure { errorMessage = it.localizedMessage }
        }
    }

    fun selectFood(food: Food) {
        selectedFood = food
        lastVerificationCode = null
        claimedPortionsInput = 1
        historyClaim = null
        errorMessage = null
        successMessage = null
    }

    fun openFromHistory(claim: Claim) {
        historyClaim = claim
        lastVerificationCode = claim.verificationCode
    }

    fun clearHistoryClaim() {
        historyClaim = null
    }

    // ========= KLAIM MAKANAN OLEH PENERIMA =========
    fun claimFood(
        food: Food,
        receiverName: String?,
        claimedPortions: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            lastVerificationCode = null

            val result = repository.processFoodClaim(food, claimedPortions, receiverName)
            isLoading = false

            result.onSuccess { code ->
                lastVerificationCode = code
                successMessage = "Klaim berhasil dibuat"
                loadReceiverFoods()
                loadReceiverHistory()
                onSuccess()
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Gagal memproses klaim"
            }
        }
    }

    // ========= VERIFIKASI KODE OLEH DONOR =========
    fun checkClaimByCode(code: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            currentClaim = null
            currentClaimFood = null

            val result = repository.getClaimByCode(code.trim())
            isLoading = false

            result.onSuccess { (claim, food) ->
                currentClaim = claim
                currentClaimFood = food
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Kode tidak ditemukan"
            }
        }
    }

    fun confirmClaimByCode(code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            val result = repository.confirmClaimByCode(code.trim())
            isLoading = false

            result.onSuccess {
                successMessage = "Klaim berhasil dikonfirmasi"
                currentClaim = null
                currentClaimFood = null
                loadDonorFoods()
                loadReceiverFoods()
                onSuccess()
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Gagal mengkonfirmasi klaim"
            }
        }
    }

    // ========= UTIL: ubah tanggal + jam jadi millis =========
    private fun mergeDateTime(date: String, timeText: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            sdf.parse("$date $timeText")?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}
