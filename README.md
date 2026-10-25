# Catatan Kontrak (Android)

Aplikasi Android untuk mencatat progres berkas kontrak pekerjaan.

## Fitur
- Halaman utama berisi kartu pekerjaan. Kartu yang terakhir diubah otomatis naik ke paling atas.
- Tombol **+** untuk menambah pekerjaan, ikon tempat sampah untuk menghapus (ada konfirmasi).
- Tiap pekerjaan punya halaman sendiri:
  - **3/4 atas**: daftar berkas (TOR, RAB, Justifikasi, Nota Dinas, SPK / Kontrak, RAB Baru, Amandemen,
    BAPP, BASTP, BAP, Nilai Kinerja, Rekap Bobot, Faktur Pajak, Permohonan Pembayaran, Kwitansi).
    - Checkbox di kanan.
    - Tombol bulat di kiri mengganti warna baris: Base → Hijau → Kuning → Base.
      Tombol ini terkunci (selalu base) sampai checkbox dicentang. Kalau centangnya dihapus, warna langsung kembali ke base.
    - **Tambah berkas** di bagian bawah daftar untuk menambah berkas custom.
    - **Tekan lama** sebuah berkas untuk menghapusnya.
  - **1/4 bawah**: kolom catatan yang tetap di tempat dan tersimpan otomatis.
- Data disimpan di HP (offline).

## Build lewat GitHub
1. Push isi folder ini ke repository GitHub (branch `main`).
2. Buka tab **Actions** → workflow **Build APK** akan berjalan otomatis.
3. Setelah selesai, buka run tersebut → bagian **Artifacts** → download `CatatanKontrak-v1.0.X` (file zip berisi APK).

## (Disarankan) Signing key tetap — lakukan sekali saja
Tanpa langkah ini, setiap build ditandatangani dengan key acak, jadi untuk update kamu harus
uninstall versi lama dulu (**data akan hilang**).

1. Tab **Actions** → **Generate Signing Key (sekali saja)** → **Run workflow** → isi password.
2. Download artifact `signing-key`, buka `PETUNJUK.txt`, lalu buat 4 secret di
   **Settings → Secrets and variables → Actions**:
   - `SIGNING_KEYSTORE_BASE64` – isi file `keystore_base64.txt`
   - `SIGNING_STORE_PASSWORD` – password tadi
   - `SIGNING_KEY_PASSWORD` – password tadi
   - `SIGNING_KEY_ALIAS` – `catatankontrak`
3. Hapus artifact `signing-key` dari halaman run, dan simpan `release.jks` sebagai cadangan.
4. Jalankan ulang **Build APK**. Mulai sekarang setiap APK bisa langsung di-install di atas versi lama.

> Kalau sebelumnya sudah install APK dengan key acak, uninstall sekali dulu sebelum install APK dengan key tetap.

## Teknologi
Kotlin, Jetpack Compose (Material 3), minSdk 26 (Android 8.0+).
