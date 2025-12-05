package com.example.santap.ui.screens.donor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.santap.data.Food
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorHistoryScreen(
    foods: List<Food>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Donasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (foods.isEmpty()) {
            //  pesan kosong
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada riwayat donasi.")
            }
        } else {
            // daftar riwayat
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(foods) { food ->
                    DonorHistoryItem(food)
                }
            }
        }
    }
}

@Composable
private fun DonorHistoryItem(food: Food) {
    // item riwayat donasi
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            Text(
                text = "Total: ${food.totalPortions} porsi • Sisa: ${food.remaining}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = food.location,
                style = MaterialTheme.typography.bodyMedium
            )

            if (food.expiresAt != 0L) {
                Text(
                    text = "Batas ambil: ${formatExpiresAt(food.expiresAt)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // status akhir donasi
            val status = when {
                food.remaining <= 0 -> "Status: Habis diambil"
                else -> "Status: Melewati batas waktu"
            }

            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

// format tanggal + waktu
private fun formatExpiresAt(expiresAt: Long): String {
    val date = Date(expiresAt)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return "${dateFormat.format(date)} • ${timeFormat.format(date)}"
}
