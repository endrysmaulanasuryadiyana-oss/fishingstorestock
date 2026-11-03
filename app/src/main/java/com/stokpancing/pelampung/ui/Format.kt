package com.stokpancing.pelampung.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localeIndonesia: Locale = Locale.forLanguageTag("id-ID")

fun formatWaktu(waktu: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", localeIndonesia).format(Date(waktu))

/** 12500 -> "12.500" */
fun formatAngka(angka: Int): String = NumberFormat.getIntegerInstance(localeIndonesia).format(angka.toLong())

/** 12500 -> "Rp12.500" */
fun formatRupiah(nilai: Long): String =
    "Rp" + NumberFormat.getIntegerInstance(localeIndonesia).format(nilai)

/** Menyisakan angka saja, dengan panjang maksimum. */
fun hanyaAngka(teks: String, maks: Int = 7): String = teks.filter { it.isDigit() }.take(maks)

fun bagikanTeks(context: Context, teks: String) {
    val kirim = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, teks)
    }
    context.startActivity(Intent.createChooser(kirim, "Bagikan laporan stok"))
}

/** Menyimpan gambar sementara lalu membuka menu bagikan (WhatsApp, dll.). */
fun bagikanGambar(context: Context, gambar: Bitmap, namaFile: String) {
    val folder = File(context.cacheDir, "nota").apply { mkdirs() }
    val file = File(folder, "$namaFile.png")
    file.outputStream().use { gambar.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    val kirim = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(kirim, "Bagikan nota"))
}
