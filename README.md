# 🍽️ SANTAP – Salur Pangan Tanpa Pemborosan

**SANTAP** adalah aplikasi Android berbasis **Jetpack Compose** untuk
menghubungkan **pendonor makanan** dan **penerima**, sehingga makanan layak konsumsi
dapat disalurkan dengan lebih efektif dan terstruktur.

Dikembangkan sebagai proyek mata kuliah  
**Pengembangan Aplikasi Perangkat Bergerak (PAPB)** – TIF,FILKOM UB.

Developer 

---

## Fitur Utama

### Autentikasi
- Login & Register menggunakan **Firebase Authentication**
- Role pengguna: **Donor** dan **Penerima**

### Pendonor
- Menambahkan donasi makanan (nama, porsi, waktu, lokasi, foto)
- Melihat donasi aktif & riwayat donasi
- Verifikasi klaim menggunakan **kode unik**
- Halaman Profile

### Penerima
- Melihat makanan yang masih tersedia
- Klaim makanan & mendapatkan kode verifikasi
- Melihat riwayat klaim
- Halaman Profile


## Arsitektur

Menggunakan pola **Model–View–ViewModel (MVVM)**:
- **Model** → logika bisnis & akses data
- **View** → UI Jetpack Compose
- **View Model** → penghubung View & Model

