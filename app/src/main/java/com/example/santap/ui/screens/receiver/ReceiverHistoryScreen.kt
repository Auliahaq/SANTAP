package com.example.santap.ui.screens.receiver

import androidx.compose.foundation.clickable
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
import com.example.santap.data.Claim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiverHistoryScreen(
    history: List<Claim>,
    onBack: () -> Unit,
    onItemClick: (Claim) -> Unit   // <- callback ketika item diklik
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val errorColor = MaterialTheme.colorScheme.error

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pengambilan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada riwayat pengambilan.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { claim ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(claim) }   // <- di-tap, kirim claim
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = claim.foodName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = claim.location,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${claim.expiryDate} • ${claim.expiryTime}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Kode: ${claim.verificationCode}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Status: ${claim.status}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = when (claim.status) {
                                    "COMPLETED" -> primaryColor
                                    "PENDING" -> secondaryColor
                                    else -> errorColor
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
