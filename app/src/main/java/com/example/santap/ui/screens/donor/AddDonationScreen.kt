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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.santap.viewmodel.FoodViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDonationScreen(
    foodViewModel: FoodViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var portions by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var expiryTime by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var isGettingLocation by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission =
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // =============== CARD DETAIL ===============
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Text("Detail Donasi", fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Makanan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = portions,
                        onValueChange = { portions = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah Porsi Tersedia") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Batas Pengambilan", fontWeight = FontWeight.SemiBold)

                    // ---------- TANGGAL ----------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        expiryDate = "%04d-%02d-%02d".format(y, m + 1, d)
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = {},
                            label = { Text("Tanggal Pengambilan") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false, // biar user nggak bisa fokus/ketik
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "Pilih tanggal"
                                )
                            }
                        )
                    }

                    // ---------- WAKTU ----------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        expiryTime = "%02d:%02d".format(h, m)
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                    ) {
                        OutlinedTextField(
                            value = expiryTime,
                            onValueChange = {},
                            label = { Text("Batas Waktu Pengambilan (contoh: 22:00)") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = "Pilih waktu"
                                )
                            }
                        )
                    }

                    Divider()

                    Text("Lokasi Pengambilan", fontWeight = FontWeight.SemiBold)

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
                                        fusedLocationClient = fusedLocationClient
                                    ) { addr ->
                                        locationText = addr
                                        isGettingLocation = false
                                    }
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
                        onValueChange = {},
                        label = { Text("Alamat / Lokasi Pengambilan") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                }
            }

            // =============== CARD FOTO ===============
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text("Foto Makanan", fontWeight = FontWeight.SemiBold)

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
                                onImageCaptureCreated = { capture -> imageCapture = capture }
                            )
                        }

                        Button(
                            onClick = {
                                val capture = imageCapture ?: return@Button
                                val nameFile = "santap_${System.currentTimeMillis()}"
                                val cv = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, nameFile)
                                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                }
                                val opts = ImageCapture.OutputFileOptions.Builder(
                                    context.contentResolver,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    cv
                                ).build()

                                capture.takePicture(
                                    opts,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onError(exc: ImageCaptureException) {
                                            // optional: tampilkan snackbar / error
                                        }

                                        override fun onImageSaved(out: ImageCapture.OutputFileResults) {
                                            imageUri = out.savedUri
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Ambil Foto")
                        }
                    } else {
                        Text(
                            "Izin kamera belum diberikan",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (imageUri != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Foto berhasil diambil.")
                        }
                    }
                }
            }

            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            if (success != null) {
                Text(success, color = MaterialTheme.colorScheme.primary)
            }

            val canSubmit = name.isNotBlank() &&
                    (portions.toIntOrNull()?.let { it > 0 } == true) &&
                    expiryDate.isNotBlank() &&
                    expiryTime.isNotBlank() &&
                    !isLoading

            Button(
                onClick = {
                    foodViewModel.addDonation(
                        context = context,
                        name = name,
                        totalPortions = portions.toInt(),
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
                shape = RoundedCornerShape(50)
            ) {
                Text(if (isLoading) "Menyimpan..." else "Simpan Donasi")
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
        .addOnSuccessListener { loc ->
            if (loc != null) {
                val geo = android.location.Geocoder(context, Locale.getDefault())
                val list = geo.getFromLocation(loc.latitude, loc.longitude, 1)
                val addr = list?.firstOrNull()?.getAddressLine(0) ?: "Lokasi tidak diketahui"
                onResult(addr)
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
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                val capture = ImageCapture.Builder().build()
                onImageCaptureCreated(capture)

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        capture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}
