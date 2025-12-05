package com.example.santap.ui.screens.donor

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.santap.viewmodel.FoodViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDonationScreen(
    foodViewModel: FoodViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // STATE FORM
    var name by remember { mutableStateOf("") }
    var portions by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }   // yyyy-MM-dd
    var expiryTime by remember { mutableStateOf("") }   // HH:mm
    var locationText by remember { mutableStateOf("") }
    var isGettingLocation by remember { mutableStateOf(false) }

    // FOTO
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // PERMISSIONS
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission =
            (result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
    }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val isLoading = foodViewModel.isLoading
    val error = foodViewModel.errorMessage
    val success = foodViewModel.successMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Donasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // CARD UTAMA FORM
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Text(
                            text = "Detail Donasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // NAMA MAKANAN
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Makanan") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // JUMLAH PORSI
                        OutlinedTextField(
                            value = portions,
                            onValueChange = { portions = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Jumlah Porsi Tersedia") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // TANGGAL & JAM PENGAMBILAN
                        Text(
                            text = "Batas Pengambilan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // TANGGAL
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val cal = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            expiryDate = String.format(
                                                "%04d-%02d-%02d",
                                                year,
                                                month + 1,
                                                dayOfMonth
                                            )
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                        ) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { },
                                label = { Text("Tanggal Pengambilan") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Pilih tanggal"
                                    )
                                }
                            )
                        }

                        // JAM
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val cal = Calendar.getInstance()
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            expiryTime = String.format(
                                                "%02d:%02d",
                                                hourOfDay,
                                                minute
                                            )
                                        },
                                        cal.get(Calendar.HOUR_OF_DAY),
                                        cal.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                }
                        ) {
                            OutlinedTextField(
                                value = expiryTime,
                                onValueChange = { },
                                label = { Text("Batas Waktu Pengambilan (contoh: 22:00)") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Pilih waktu"
                                    )
                                }
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // LOKASI
                        Text(
                            text = "Lokasi Pengambilan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Button(
                            onClick = {
                                if (!hasLocationPermission) {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else {
                                    isGettingLocation = true
                                    locationText = "Mengambil lokasi..."
                                    scope.launch {
                                        getCurrentAddress(
                                            context = context,
                                            fusedLocationClient = fusedLocationClient,
                                            onResult = { addr ->
                                                locationText = addr
                                                isGettingLocation = false
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGettingLocation,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                if (isGettingLocation)
                                    "Mengambil lokasi..."
                                else
                                    "Gunakan Lokasi Saat Ini"
                            )
                        }

                        OutlinedTextField(
                            value = locationText,
                            onValueChange = { },
                            label = { Text("Alamat / Lokasi Pengambilan") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                    }
                }

                // CARD FOTO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Foto Makanan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (hasCameraPermission) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                CameraPreview(
                                    modifier = Modifier.fillMaxSize(),
                                    onImageCaptureCreated = { imageCapture = it }
                                )
                            }

                            Button(
                                onClick = {
                                    val capture = imageCapture ?: return@Button

                                    val nameFile = "santap_${System.currentTimeMillis()}"
                                    val contentValues = ContentValues().apply {
                                        put(MediaStore.MediaColumns.DISPLAY_NAME, nameFile)
                                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                    }

                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(
                                        context.contentResolver,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        contentValues
                                    ).build()

                                    capture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onError(exc: ImageCaptureException) {
                                                // kalau mau, bisa tambahkan snackbar / state error
                                            }

                                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                imageUri = output.savedUri
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Cekrek / Ambil Foto")
                            }
                        } else {
                            Text(
                                "Izin kamera belum diberikan",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (imageUri != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(imageUri),
                                        contentDescription = "Foto makanan",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    text = "Foto berhasil diambil.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // STATUS ERROR / SUCCESS
                if (error != null) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (success != null) {
                    Text(
                        success,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Validasi form (tidak mengubah logika backend, hanya bantu enable/disable button)
                val canSubmit = remember(name, portions, expiryDate, expiryTime, isLoading) {
                    name.isNotBlank() &&
                            (portions.toIntOrNull()?.let { it > 0 } == true) &&
                            expiryDate.isNotBlank() &&
                            expiryTime.isNotBlank() &&
                            !isLoading
                }

                // BUTTON SIMPAN
                Button(
                    onClick = {
                        val porsi = portions.toInt()
                        foodViewModel.addDonation(
                            context = context,
                            name = name,
                            totalPortions = porsi,
                            expiryDate = expiryDate,
                            expiryTime = expiryTime,
                            location = locationText,
                            imageUri = imageUri
                        ) {
                            onBack()
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (isLoading) "Menyimpan..." else "Simpan Donasi")
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getCurrentAddress(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onResult: (String) -> Unit
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = android.location.Geocoder(context, Locale.getDefault())
                val list = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = list?.firstOrNull()?.getAddressLine(0) ?: "Lokasi tidak diketahui"
                onResult(address)
            } else {
                onResult("Lokasi tidak tersedia")
            }
        }
        .addOnFailureListener {
            onResult("Gagal mendapatkan lokasi")
        }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptureCreated: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val imageCapture = ImageCapture.Builder().build()
                onImageCaptureCreated(imageCapture)

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}
