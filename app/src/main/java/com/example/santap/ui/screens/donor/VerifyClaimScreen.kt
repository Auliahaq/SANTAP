package com.example.santap.ui.screens.donor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.santap.viewmodel.FoodViewModel
// Hapus import warna yang tidak terpakai/error:
// import com.example.santap.ui.theme.GreenMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyClaimScreen(
    foodViewModel: FoodViewModel,
    onBack: () -> Unit
) {
    var codeText by remember { mutableStateOf("") }

    val isLoading = foodViewModel.isLoading
    val error = foodViewModel.errorMessage
    val success = foodViewModel.successMessage
    val claim = foodViewModel.currentClaim
    val food = foodViewModel.currentClaimFood

    // Warna dari Material Theme:
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary // Digunakan untuk tombol kritis

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verifikasi Klaim") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = codeText,
                onValueChange = { codeText = it },
                label = { Text("Kode Verifikasi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { foodViewModel.checkClaimByCode(codeText) },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    // Tombol 'Cek Kode' menggunakan Primary Color
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Cek Kode")
                }

                Button(
                    onClick = {
                        foodViewModel.confirmClaimByCode(codeText) {
                            codeText = ""
                        }
                    },
                    enabled = !isLoading && claim != null && food != null,
                    modifier = Modifier.weight(1f),
                    // Tombol 'Konfirmasi' (Aksi Kritis) menggunakan Secondary Color (Amber/Mustard)
                    colors = ButtonDefaults.buttonColors(
                        containerColor = secondaryColor
                    )
                ) {
                    Text("Konfirmasi")
                }
            }

            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            if (success != null) {
                // Mengganti GreenMain dengan primaryColor
                Text(success, color = primaryColor, fontWeight = FontWeight.SemiBold)
            }

            if (claim != null && food != null) {
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        // Mengganti GreenMain dengan primaryColor untuk highlight
                        Text("Kode: ${claim.verificationCode}", color = primaryColor, fontWeight = FontWeight.Bold)
                        Text("Penerima: ${claim.receiverName}")
                        Text("Lokasi: ${claim.location}")
                        Text("Waktu Ambil: ${claim.expiryDate} • ${claim.expiryTime}")
                    }
                }
            }
        }
    }
}