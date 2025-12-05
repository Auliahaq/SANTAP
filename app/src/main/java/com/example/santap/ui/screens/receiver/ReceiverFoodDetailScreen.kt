package com.example.santap.ui.screens.receiver

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.santap.data.Food

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiverFoodDetailScreen(
    food: Food,
    isLoading: Boolean,
    error: String?,
    verificationCode: String?,
    onBack: () -> Unit,
    onClaimClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Makanan") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {

                    // foto makanan
                    if (food.photoUrl.isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(food.photoUrl),
                            contentDescription = "Foto makanan",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Restaurant,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // detail teks
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Restaurant, null, tint = primaryColor)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = food.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text("${food.remaining} porsi tersedia", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, null, tint = secondaryColor)
                            Spacer(Modifier.width(8.dp))
                            Text(food.location, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AccessTime, null, tint = primaryColor)
                            Spacer(Modifier.width(8.dp))
                            Text("${food.expiryDate} • ${food.expiryTime}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // pesan error
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            // kartu kode verifikasi
            if (!verificationCode.isNullOrBlank()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Kode Klaim Unik",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            verificationCode,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tunjukkan kode ini ke pendonor saat mengambil makanan.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // tombol ambil
            Button(
                onClick = onClaimClick,
                enabled = !isLoading && verificationCode.isNullOrBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(if (isLoading) "Memproses..." else "Ambil Makanan")
            }
        }
    }
}
