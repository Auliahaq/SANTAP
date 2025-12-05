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
    // FoodRepository sudah pakai Supabase untuk foto + Firestore untuk data
    private val repository: FoodRepository = FoodRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    // ---------- Donor ----------

    var donorFoods by mutableStateOf<List<Food>>(emptyList())
        private set

    var donorHistory by mutableStateOf<List<Food>>(emptyList())
        private set

    // ---------- Penerima ----------

    var receiverFoods by mutableStateOf<List<Food>>(emptyList())
        private set

    var receiverHistory by mutableStateOf<List<Claim>>(emptyList())
        private set

    var selectedFood by mutableStateOf<Food?>(null)
        private set

    // porsi yang diinput penerima
    var claimedPortionsInput by mutableStateOf(0)
        private set

    var lastVerificationCode by mutableStateOf<String?>(null)
        private set

    // claim yang sedang dicek donor
    var currentClaim by mutableStateOf<Claim?>(null)
        private set

    var currentClaimFood by mutableStateOf<Food?>(null)
        private set
    var historyClaim by mutableStateOf<Claim?>(null)
        private set

    // --------------------------------------------------------------------
    // DONOR: TAMBAH DONASI
    // --------------------------------------------------------------------
    fun addDonation(
        context: Context,
        name: String,
        totalPortions: Int,
        expiryDate: String,
        expiryTime: String,
        location: String,
        imageUri: Uri?,
        onSuccessNavigate: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            // Gabungkan tanggal + jam -> millis
            val expiresAt = mergeDateTime(expiryDate, expiryTime)
            val now = System.currentTimeMillis()

            if (expiresAt <= now) {
                // Batas waktu sudah lewat → blok
                isLoading = false
                errorMessage = "Batas pengambilan tidak boleh di waktu yang sudah lewat."
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
                imageUri = imageUri
            )

            isLoading = false

            result
                .onSuccess {
                    successMessage = "Donasi berhasil diposting"
                    loadDonorFoods()
                    onSuccessNavigate()
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "Gagal menyimpan donasi"
                }
        }
    }

    // --------------------------------------------------------------------
    // DONOR: LIST & HISTORY
    // --------------------------------------------------------------------
    fun loadDonorFoods() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.getFoodsForDonor()

            isLoading = false

            result
                .onSuccess { list ->
                    val now = System.currentTimeMillis()
                    // Tambahan filter jaga-jaga: hanya yang belum lewat batas
                    donorFoods = list.filter { it.expiresAt > now }
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "Gagal mengambil data donasi"
                }
        }
    }

    fun loadDonorHistory() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.getDonorHistory()

            isLoading = false

            result
                .onSuccess { list ->
                    donorHistory = list
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "Gagal mengambil riwayat donasi"
                }
        }
    }

    // --------------------------------------------------------------------
    // PENERIMA: LIST, DETAIL, HISTORY
    // --------------------------------------------------------------------
    fun loadReceiverFoods() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.getFoodsForReceiver()

            isLoading = false

            result
                .onSuccess { list ->
                    val now = System.currentTimeMillis()
                    // Jaga-jaga: filter lagi hanya yang aktif
                    receiverFoods = list.filter { it.expiresAt > now }
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "Gagal mengambil data makanan"
                }
        }
    }

    fun loadReceiverHistory() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.getHistoryForReceiver()

            isLoading = false

            result
                .onSuccess { list ->
                    receiverHistory = list
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "Gagal mengambil riwayat"
                }
        }
    }

    fun selectFood(food: Food) {
        selectedFood = food
        errorMessage = null
        successMessage = null
        lastVerificationCode = null      // reset kode kalau bukan dari riwayat
        claimedPortionsInput = 1
        historyClaim = null              // reset flag "dibuka dari riwayat"
    }
    // Dipanggil ketika user klik item di halaman riwayat
    fun openFromHistory(claim: Claim) {
        historyClaim = claim
        // Isi lastVerificationCode supaya di detail langsung tampil kode
        lastVerificationCode = claim.verificationCode
    }

    // Opsional: dipanggil saat back dari detail
    fun clearHistoryClaim() {
        historyClaim = null
    }


    fun updateClaimedPortionsInput(portions: Int) {
        claimedPortionsInput = portions
    }

    // --------------------------------------------------------------------
    // PENERIMA: KLAIM MAKANAN
    // --------------------------------------------------------------------
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

            val result = repository.processFoodClaim(
                food = food,
                claimedPortions = claimedPortions,
                receiverName = receiverName
            )

            isLoading = false

            result
                .onSuccess { code ->
                    lastVerificationCode = code
                    successMessage = "Klaim berhasil dibuat. Tunjukkan kode ini ke pendonor."
                    loadReceiverFoods()
                    loadReceiverHistory()
                    onSuccess()
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "Gagal memproses klaim"
                }
        }
    }

    // --------------------------------------------------------------------
    // DONOR: VERIFIKASI KODE
    // --------------------------------------------------------------------
    fun checkClaimByCode(code: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            currentClaim = null
            currentClaimFood = null

            val result = repository.getClaimByCode(code.trim())

            isLoading = false

            result
                .onSuccess { (claim, food) ->
                    currentClaim = claim
                    currentClaimFood = food
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "Kode tidak ditemukan"
                }
        }
    }

    fun confirmClaimByCode(
        code: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            val result = repository.confirmClaimByCode(code.trim())

            isLoading = false

            result
                .onSuccess {
                    successMessage = "Klaim berhasil dikonfirmasi."
                    currentClaim = null
                    currentClaimFood = null

                    loadDonorFoods()
                    loadReceiverFoods()

                    onSuccess()
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "Gagal mengkonfirmasi klaim"
                }
        }
    }

    // --------------------------------------------------------------------
    // Helper: gabung tanggal + jam → millis
    // --------------------------------------------------------------------
    private fun mergeDateTime(date: String, timeText: String): Long {
        return try {
            val text = "$date $timeText"           // "2025-11-25 22:00"
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            val parsed = sdf.parse(text)
            parsed?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
