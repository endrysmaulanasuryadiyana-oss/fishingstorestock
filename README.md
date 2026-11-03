# Stok Pelampung Pancing (Aplikasi Android)

Aplikasi untuk mencatat stok pelampung pancing di gudang: **barang masuk**, **barang keluar**,
dan **jumlah yang tersisa di gudang** (dihitung otomatis). Data tersimpan di HP, tidak perlu internet.

## Fitur

- **Daftar barang**: nama, harga jual, total stok masuk, total barang keluar, dan jumlah di gudang.
- **Logo & nama toko** di atas daftar barang. Ketuk kotak logo untuk memilih gambar dari galeri.
- **Harga beli (modal) & harga jual** per barang, nilai stok gudang, dan untung per pcs.
- **Barang Masuk / Barang Keluar** manual dengan catatan. Barang keluar tidak bisa melebihi stok.
- **Menu Nota**: pilih barang + jumlah, simpan nota, dan stok otomatis berkurang.
  Nomor nota otomatis (NT-tanggal-urut). Nota bisa **dibagikan sebagai gambar** ke WhatsApp.
  Menghapus nota akan mengembalikan stok. Ada ringkasan penjualan hari ini.
- **Menu Toko**: nama toko, alamat, no. telepon/WA, website, tulisan bawah nota, dan logo
  (semuanya tampil di kop nota).
- **Riwayat** semua catatan masuk/keluar, bisa disaring.
- **Bagikan laporan stok** sebagai teks (ikon bagikan di kanan atas menu Stok).

Data dari versi 1.0 tetap aman saat diperbarui ke versi 1.1.

Syarat: Android 8.0 ke atas.

---

## Cara 1: Dapatkan APK lewat GitHub (tanpa memasang apa pun di komputer)

1. Buat akun di https://github.com (gratis), lalu buat repository baru, misalnya `stok-pelampung`.
2. Di halaman repository, klik **Add file → Upload files**, lalu tarik **seluruh isi** folder ini
   (termasuk folder `.github`, `app`, `gradle`, dan file `gradlew`). Klik **Commit changes**.
   > Folder `.github` kadang tersembunyi. Jika tidak ikut terunggah, buat file
   > `.github/workflows/build-apk.yml` lewat **Add file → Create new file** lalu salin isinya.
3. Buka tab **Actions**. Proses "Build APK" akan berjalan sekitar 5–10 menit.
4. Setelah selesai (tanda centang hijau), klik proses tersebut. Di bagian **Artifacts**,
   unduh **StokPelampung-apk**, lalu ekstrak file zip-nya untuk mendapatkan `app-debug.apk`.
5. Kirim `app-debug.apk` ke HP dan buka. Jika diminta, izinkan "Instal dari sumber tidak dikenal".

## Cara 2: Build dengan Android Studio

1. Pasang Android Studio dari https://developer.android.com/studio.
2. Pilih **File → Open**, lalu pilih folder `StokPelampung` ini. Tunggu proses "Gradle sync" selesai
   (perlu internet saat pertama kali).
3. Sambungkan HP (aktifkan *USB debugging*) atau pakai emulator, lalu tekan tombol **Run ▶**.
4. Untuk file APK: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
   Hasilnya ada di `app/build/outputs/apk/debug/app-debug.apk`.

---

## Struktur kode

| File | Isi |
|------|-----|
| `data/Models.kt` | Data Barang & Transaksi; sisa = total masuk − total keluar |
| `data/StokRepository.kt` | Database SQLite di HP |
| `ui/StokViewModel.kt` | Logika aplikasi & teks laporan |
| `MainActivity.kt` | Layar utama dan navigasi tab |
| `ui/BerandaScreen.kt` | Daftar stok dan ringkasan gudang |
| `ui/DetailScreen.kt` | Detail barang, tombol masuk/keluar, riwayat per barang |
| `ui/RiwayatScreen.kt` | Riwayat semua transaksi |
| `ui/Dialogs.kt` | Form tambah/ubah barang dan form masuk/keluar |

Dibuat dengan Kotlin + Jetpack Compose (Material 3).
