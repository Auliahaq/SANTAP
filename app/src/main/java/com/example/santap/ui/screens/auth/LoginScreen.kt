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

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccessDonor: () -> Unit,
    onLoginSuccessReceiver: () -> Unit,
    onBack: () -> Unit
) {
    // nyimpen input email & password
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // cek status login dari ViewModel
    val isLoading = authViewModel.isLoading
    val error = authViewModel.errorMessage

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // card tempat form login
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset(y = (-350).dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Login SANTAP",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Masuk untuk melanjutkan donasi atau menerima makanan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // input email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // input password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    // kalau login gagal, error muncul di sini
                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // aksi login
                    Button(
                        onClick = {
                            authViewModel.login(email, password) { role ->
                                if (role == "donor") onLoginSuccessDonor()
                                else onLoginSuccessReceiver()
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = if (isLoading) "Loading..." else "Login",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // tombol balik
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text("Kembali")
                    }
                }
            }
        }
    }
}
