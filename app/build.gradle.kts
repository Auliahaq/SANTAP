plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.santap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.santap"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true

    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}   // <- INI penting, penutup blok android

dependencies {

    // --- Compose & AndroidX dasar ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- Firebase ---
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- Tambahan manual ---

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // ViewModel untuk Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Ikon Material
    implementation("androidx.compose.material:material-icons-extended")

    // Lokasi (GPS)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Untuk .await() di Firebase + Location
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // Firebase BoM (boleh, ini buat sinkron versi)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Storage Firebase — bisa DIHAPUS kalau kamu sudah full pindah ke Supabase
    implementation("com.google.firebase:firebase-storage-ktx")

    // CameraX
    val cameraVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")

    // Accompanist permissions
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // Coil untuk load gambar
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("com.google.guava:guava:31.1-android")

    // 🔹 Desugaring buat minSdk < 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Supabase BOM + storage module
    implementation(platform("io.github.jan-tennert.supabase:bom:2.3.1"))
    implementation("io.github.jan-tennert.supabase:storage-kt")

// Wajib untuk Supabase-kt
    implementation("io.ktor:ktor-client-android:2.3.9")


}
