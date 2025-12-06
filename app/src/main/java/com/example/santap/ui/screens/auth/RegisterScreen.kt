package com.example.santap.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.santap.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    // state input form
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("receiver") } // donor / receiver

    // status dari ViewModel
    val isLoading = authViewModel.isLoading
    val error = authViewModel.errorMessage

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // card utama tempat form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset(y = (-360).dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daftar Akun SANTAP",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Buat akun untuk mulai berdonasi atau menerima surplus pangan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // input nama
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // input nomor telepon
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("No. Telepon") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // input email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // input password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // pilih role akun
                    Text("Daftar sebagai:")

                    Spacer(modifier = Modifier.height(12.dp))


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedRole == "donor",
                            onClick = { selectedRole = "donor" },
                            label = { Text("Donor") }
                        )
                        FilterChip(
                            selected = selectedRole == "receiver",
                            onClick = { selectedRole = "receiver" },
                            label = { Text("Penerima") }
                        )
                    }

                    // tampilin error kalau ada
                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // tombol daftar → lanjut kalau sukses
                    Button(
                        onClick = {
                            authViewModel.register(
                                name = name,
                                phone = phone,
                                email = email,
                                password = password,
                                role = selectedRole,
                                onSuccess = onRegisterSuccess
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = if (isLoading) "Daftar..." else "Daftar",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // balik ke halaman sebelumnya
                    TextButton(onClick = onBack) {
                        Text("Kembali")
                    }
                }
            }
        }
    }
}