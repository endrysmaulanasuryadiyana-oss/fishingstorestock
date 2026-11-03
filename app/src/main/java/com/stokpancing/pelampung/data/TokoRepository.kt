package com.stokpancing.pelampung.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/** Menyimpan profil toko (SharedPreferences) dan logo toko (file di memori aplikasi). */
class TokoRepository(context: Context) {

    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("profil_toko", Context.MODE_PRIVATE)
    private val fileLogo: File get() = File(app.filesDir, "logo_toko.png")

    fun baca(): TokoInfo {
        val bawaan = TokoInfo()
        return TokoInfo(
            nama = prefs.getString("nama", null) ?: bawaan.nama,
            alamat = prefs.getString("alamat", null) ?: bawaan.alamat,
            telepon = prefs.getString("telepon", null) ?: bawaan.telepon,
            website = prefs.getString("website", null) ?: bawaan.website,
            catatanKaki = prefs.getString("catatan_kaki", null) ?: bawaan.catatanKaki,
        )
    }

    fun simpan(info: TokoInfo) {
        prefs.edit()
            .putString("nama", info.nama)
            .putString("alamat", info.alamat)
            .putString("telepon", info.telepon)
            .putString("website", info.website)
            .putString("catatan_kaki", info.catatanKaki)
            .apply()
    }

    suspend fun bacaLogo(): Bitmap? = withContext(Dispatchers.IO) {
        val f = fileLogo
        if (f.exists()) BitmapFactory.decodeFile(f.path) else null
    }

    /** Menyalin gambar dari galeri ke memori aplikasi (diperkecil maks. 512 px). */
    suspend fun simpanLogo(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        val resolver = app.contentResolver
        val ukuran = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, ukuran) }
        if (ukuran.outWidth <= 0 || ukuran.outHeight <= 0) error("Gambar tidak dapat dibaca")

        var sampel = 1
        while (max(ukuran.outWidth, ukuran.outHeight) / (sampel * 2) >= 512) sampel *= 2
        val opsi = BitmapFactory.Options().apply { inSampleSize = sampel }
        val asli = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opsi) }
            ?: error("Gambar tidak dapat dibaca")

        val skala = 512f / max(asli.width, asli.height)
        val logo = if (skala < 1f) {
            Bitmap.createScaledBitmap(
                asli,
                (asli.width * skala).roundToInt().coerceAtLeast(1),
                (asli.height * skala).roundToInt().coerceAtLeast(1),
                true,
            )
        } else {
            asli
        }
        fileLogo.outputStream().use { logo.compress(Bitmap.CompressFormat.PNG, 100, it) }
        logo
    }

    suspend fun hapusLogo() = withContext(Dispatchers.IO) {
        fileLogo.delete()
    }
}
