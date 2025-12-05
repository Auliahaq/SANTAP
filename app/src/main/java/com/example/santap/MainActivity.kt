package com.example.santap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.santap.ui.screens.auth.LoginScreen
import com.example.santap.ui.screens.auth.RegisterScreen
import com.example.santap.ui.screens.auth.WelcomeScreen
import com.example.santap.ui.screens.donor.AddDonationScreen
import com.example.santap.ui.screens.donor.DashboardDonorScreen
import com.example.santap.ui.screens.donor.DonorHistoryScreen
import com.example.santap.ui.screens.donor.VerifyClaimScreen
import com.example.santap.ui.screens.profile.ProfileScreen
import com.example.santap.ui.screens.receiver.HomeReceiverScreen
import com.example.santap.ui.screens.receiver.ReceiverFoodDetailScreen
import com.example.santap.ui.screens.receiver.ReceiverHistoryScreen
import com.example.santap.ui.theme.SANTAPTheme
import com.example.santap.viewmodel.AuthViewModel
import com.example.santap.viewmodel.FoodViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SANTAPTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val foodViewModel: FoodViewModel = viewModel()

                    SantapNavHost(
                        navController = navController,
                        authViewModel = authViewModel,
                        foodViewModel = foodViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun SantapNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    foodViewModel: FoodViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {

        // ---------------- AUTH ----------------
        composable("welcome") {
            WelcomeScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccessDonor = {
                    foodViewModel.loadDonorFoods()
                    navController.navigate("dashboard_donor") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onLoginSuccessReceiver = {
                    foodViewModel.loadReceiverFoods()
                    navController.navigate("home_receiver") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    val role = authViewModel.currentUser?.role
                    if (role == "donor") {
                        foodViewModel.loadDonorFoods()
                        navController.navigate("dashboard_donor") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    } else {
                        foodViewModel.loadReceiverFoods()
                        navController.navigate("home_receiver") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- DONOR ----------------
        composable("dashboard_donor") {
            LaunchedEffect(Unit) {
                foodViewModel.loadDonorFoods()
            }

            DashboardDonorScreen(
                foods = foodViewModel.donorFoods,
                userName = authViewModel.currentUser?.name ?: "Donor",
                onAddDonation = { navController.navigate("add_donation") },
                onVerifyClaim = { navController.navigate("verify_claim") },
                onHistoryClick = {
                    foodViewModel.loadDonorHistory()
                    navController.navigate("donor_history")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                isRefreshing = foodViewModel.isLoading,
                onRefresh = {
                    foodViewModel.loadDonorFoods()
                }
            )
        }

        composable("donor_history") {
            LaunchedEffect(Unit) {
                foodViewModel.loadDonorHistory()
            }

            DonorHistoryScreen(
                foods = foodViewModel.donorHistory,
                onBack = { navController.popBackStack() }
            )
        }

        composable("add_donation") {
            AddDonationScreen(
                foodViewModel = foodViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("verify_claim") {
            VerifyClaimScreen(
                foodViewModel = foodViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- RECEIVER ----------------
        composable("home_receiver") {
            LaunchedEffect(Unit) {
                foodViewModel.loadReceiverFoods()
            }

            HomeReceiverScreen(
                foods = foodViewModel.receiverFoods,
                userName = authViewModel.currentUser?.name ?: "Penerima",
                onFoodClick = { food ->
                    foodViewModel.selectFood(food)
                    navController.navigate("receiver_detail")
                },
                onHistoryClick = {
                    foodViewModel.loadReceiverHistory()
                    navController.navigate("receiver_history")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                isRefreshing = foodViewModel.isLoading,
                onRefresh = {
                    // ⬅️ untuk penerima harus loadReceiverFoods
                    foodViewModel.loadReceiverFoods()
                }
            )
        }

        composable("receiver_detail") {
            val food = foodViewModel.selectedFood
            val claimedPortions = foodViewModel.claimedPortionsInput

            if (food == null) {
                Text("Data makanan tidak tersedia")
            } else {
                ReceiverFoodDetailScreen(
                    food = food,
                    isLoading = foodViewModel.isLoading,
                    error = foodViewModel.errorMessage,
                    verificationCode = foodViewModel.lastVerificationCode,
                    onBack = {
                        foodViewModel.clearHistoryClaim()
                        navController.popBackStack()
                    },
                    onClaimClick = {
                        foodViewModel.claimFood(
                            food = food,
                            receiverName = authViewModel.currentUser?.name,
                            claimedPortions = claimedPortions
                        ) {
                            // tetap di halaman ini supaya kode verifikasi tampil
                        }
                    }
                )
            }
        }

        composable("receiver_history") {
            LaunchedEffect(Unit) {
                foodViewModel.loadReceiverHistory()
                foodViewModel.loadReceiverFoods()
            }

            ReceiverHistoryScreen(
                history = foodViewModel.receiverHistory,
                onBack = { navController.popBackStack() },
                onItemClick = { claim ->
                    val food = foodViewModel.receiverFoods.find { it.id == claim.foodId }
                    if (food != null) {
                        foodViewModel.selectFood(food)
                        foodViewModel.openFromHistory(claim)
                        navController.navigate("receiver_detail")
                    }
                }
            )
        }

        // ---------------- PROFILE ----------------
        composable("profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
    }
}
