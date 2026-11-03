package com.stokpancing.pelampung.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.stokpancing.pelampung.data.Nota
import com.stokpancing.pelampung.data.TokoInfo
import kotlin.math.ceil
import kotlin.math.min

/**
 * Menggambar nota menjadi gambar (Bitmap) bergaya struk.
 * Gambar yang sama dipakai untuk pratinjau di layar dan untuk dibagikan.
 */
object NotaGambar {

    private const val LEBAR = 720
    private const val TEPI = 44f
    private val LEBAR_ISI = (LEBAR - 2 * TEPI).toInt()

    private val warnaTeks = Color.rgb(33, 33, 33)
    private val warnaAbu = Color.rgb(97, 97, 97)

    fun buat(nota: Nota, toko: TokoInfo, logo: Bitmap?): Bitmap {
        // Tahap 1: hitung tinggi. Tahap 2: gambar sungguhan.
        val ukur = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val tinggi = gambar(Canvas(ukur), nota, toko, logo)
        val hasil = Bitmap.createBitmap(LEBAR, ceil(tinggi).toInt(), Bitmap.Config.ARGB_8888)
        gambar(Canvas(hasil), nota, toko, logo)
        return hasil
    }

    private fun cat(ukuran: Float, tebal: Boolean = false, warna: Int = warnaTeks, miring: Boolean = false) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = ukuran
            color = warna
            typeface = Typeface.create(
                Typeface.DEFAULT,
                when {
                    tebal && miring -> Typeface.BOLD_ITALIC
                    tebal -> Typeface.BOLD
                    miring -> Typeface.ITALIC
                    else -> Typeface.NORMAL
                },
            )
        }

    private fun gambar(c: Canvas, nota: Nota, toko: TokoInfo, logo: Bitmap?): Float {
        c.drawColor(Color.WHITE)
        var y = TEPI

        // ---- Kop toko ----
        if (logo != null) {
            val skala = min(150f / logo.height, 320f / logo.width)
            val w = logo.width * skala
            val h = logo.height * skala
            val kiri = (LEBAR - w) / 2
            c.drawBitmap(logo, null, RectF(kiri, y, kiri + w, y + h), Paint(Paint.FILTER_BITMAP_FLAG))
            y += h + 16f
        }
        y = teks(c, toko.nama.ifBlank { "Toko Pelampung Pancing" }, cat(40f, tebal = true), Layout.Alignment.ALIGN_CENTER, y)
        val sedang = cat(24f, warna = warnaAbu)
        if (toko.alamat.isNotBlank()) y = teks(c, toko.alamat, sedang, Layout.Alignment.ALIGN_CENTER, y + 4f)
        if (toko.telepon.isNotBlank()) y = teks(c, "Telp/WA: ${toko.telepon}", sedang, Layout.Alignment.ALIGN_CENTER, y + 2f)
        if (toko.website.isNotBlank()) y = teks(c, toko.website, sedang, Layout.Alignment.ALIGN_CENTER, y + 2f)

        y += 18f
        garis(c, y, putus = false)
        y += 14f
        y = teks(c, "NOTA PENJUALAN", cat(28f, tebal = true), Layout.Alignment.ALIGN_CENTER, y)
        y += 10f

        // ---- Info nota ----
        val info = cat(24f)
        y = baris(c, "No. Nota", nota.nomor, info, info, y)
        y = baris(c, "Tanggal", formatWaktu(nota.waktu), info, info, y)
        if (nota.pelanggan.isNotBlank()) y = baris(c, "Pelanggan", nota.pelanggan, info, info, y)

        y += 12f
        garis(c, y, putus = true)
        y += 16f

        // ---- Daftar barang ----
        val namaBarang = cat(26f, tebal = true)
        val rincian = cat(24f, warna = warnaAbu)
        val subtotal = cat(25f)
        nota.items.forEach { item ->
            y = teks(c, item.namaBarang, namaBarang, Layout.Alignment.ALIGN_NORMAL, y)
            y = baris(
                c,
                "${formatAngka(item.jumlah)} x ${formatRupiah(item.harga)}",
                formatRupiah(item.subtotal),
                rincian, subtotal, y + 2f,
            )
            y += 12f
        }

        garis(c, y, putus = true)
        y += 16f
        y = baris(c, "Jumlah barang", "${formatAngka(nota.jumlahBarang)} pcs", info, info, y)
        y += 4f
        val besar = cat(32f, tebal = true)
        y = baris(c, "TOTAL", formatRupiah(nota.total), besar, besar, y)

        if (nota.catatan.isNotBlank()) {
            y += 16f
            y = teks(c, "Catatan: ${nota.catatan}", cat(23f, warna = warnaAbu), Layout.Alignment.ALIGN_NORMAL, y)
        }

        y += 18f
        garis(c, y, putus = false)
        y += 18f
        if (toko.catatanKaki.isNotBlank()) {
            y = teks(c, toko.catatanKaki, cat(24f, miring = true, warna = warnaAbu), Layout.Alignment.ALIGN_CENTER, y)
        }
        return y + TEPI
    }

    /** Teks yang otomatis turun baris jika terlalu panjang. */
    private fun teks(c: Canvas, isi: String, cat: TextPaint, rata: Layout.Alignment, y: Float): Float {
        val layout = StaticLayout.Builder.obtain(isi, 0, isi.length, cat, LEBAR_ISI)
            .setAlignment(rata)
            .build()
        c.save()
        c.translate(TEPI, y)
        layout.draw(c)
        c.restore()
        return y + layout.height
    }

    /** Satu baris: teks rata kiri dan teks rata kanan. */
    private fun baris(c: Canvas, kiri: String, kanan: String, catKiri: TextPaint, catKanan: TextPaint, y: Float): Float {
        val dasar = y - min(catKiri.ascent(), catKanan.ascent())
        catKiri.textAlign = Paint.Align.LEFT
        c.drawText(kiri, TEPI, dasar, catKiri)
        catKanan.textAlign = Paint.Align.RIGHT
        c.drawText(kanan, LEBAR - TEPI, dasar, catKanan)
        catKanan.textAlign = Paint.Align.LEFT
        return y + maxOf(catKiri.fontSpacing, catKanan.fontSpacing)
    }

    private fun garis(c: Canvas, y: Float, putus: Boolean) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (putus) Color.rgb(160, 160, 160) else warnaTeks
            strokeWidth = if (putus) 2f else 3f
            if (putus) pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
        }
        c.drawLine(TEPI, y, LEBAR - TEPI, y, p)
    }
}
