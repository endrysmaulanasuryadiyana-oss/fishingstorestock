package com.stokpancing.pelampung.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stokpancing.pelampung.data.Barang
import com.stokpancing.pelampung.data.JenisTransaksi
import com.stokpancing.pelampung.data.TokoInfo
import com.stokpancing.pelampung.data.Transaksi

/** Kotak logo toko. Jika belum ada logo, tampil tombol "+ Logo". */
@Composable
fun LogoToko(logo: Bitmap?, ukuran: Dp, onKlik: (() -> Unit)?) {
    val dasar = Modifier
        .size(ukuran)
        .clip(RoundedCornerShape(14.dp))
        .background(if (logo == null) MaterialTheme.colorScheme.primaryContainer else Color.White)
    val mod = if (onKlik != null) dasar.clickable(onClick = onKlik) else dasar
    Box(mod, contentAlignment = Alignment.Center) {
        if (logo != null) {
            val gambar = remember(logo) { logo.asImageBitmap() }
            Image(
                bitmap = gambar,
                contentDescription = "Logo toko",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Logo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** Kop toko di atas daftar barang: logo + nama + alamat. */
@Composable
fun KopToko(toko: TokoInfo, logo: Bitmap?, onPilihLogo: () -> Unit, onAturToko: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            LogoToko(logo, 68.dp, onPilihLogo)
            Spacer(Modifier.width(14.dp))
            Column(
                Modifier
                    .weight(1f)
                    .clickable(onClick = onAturToko),
            ) {
                Text(
                    toko.nama.ifBlank { "Nama toko belum diatur" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (toko.nama.isBlank()) Color.Gray else Color.Unspecified,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val detail = listOf(toko.alamat, toko.telepon).filter { it.isNotBlank() }.joinToString(" • ")
                if (detail.isNotBlank()) {
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (logo == null || toko.nama.isBlank()) {
                    Text(
                        if (logo == null) "Ketuk kotak logo untuk unggah dari galeri" else "Ketuk untuk atur profil toko",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

/** Label kecil + angka besar, dipakai untuk Masuk / Keluar / Di Gudang. */
@Composable
fun KolomAngka(
    label: String,
    nilai: Int,
    warna: Color,
    modifier: Modifier = Modifier,
    tebal: Boolean = false,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(
            formatAngka(nilai),
            style = if (tebal) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
            fontWeight = if (tebal) FontWeight.Bold else FontWeight.SemiBold,
            color = warna,
        )
    }
}

@Composable
fun LabelMenipis() {
    Surface(color = WarnaMenipis, shape = RoundedCornerShape(50)) {
        Text(
            "Stok menipis",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
fun KartuBarang(barang: Barang, onKlik: () -> Unit) {
    Card(
        onClick = onKlik,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        barang.nama,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (barang.keterangan.isNotBlank()) {
                        Text(
                            barang.keterangan,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        if (barang.hargaJual > 0) "Harga jual ${formatRupiah(barang.hargaJual)}" else "Harga belum diatur",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (barang.hargaJual > 0) MaterialTheme.colorScheme.secondary else Color.Gray,
                    )
                }
                if (barang.menipis) {
                    Spacer(Modifier.width(8.dp))
                    LabelMenipis()
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                KolomAngka("Stok Masuk", barang.totalMasuk, WarnaMasuk, Modifier.weight(1f))
                KolomAngka("Barang Keluar", barang.totalKeluar, WarnaKeluar, Modifier.weight(1f))
                KolomAngka(
                    "Di Gudang",
                    barang.sisa,
                    if (barang.menipis) WarnaMenipis else MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f),
                    tebal = true,
                )
            }
        }
    }
}

@Composable
fun BarisTransaksi(
    transaksi: Transaksi,
    tampilkanNama: Boolean,
    onHapus: (() -> Unit)? = null,
) {
    val masuk = transaksi.jenis == JenisTransaksi.MASUK
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                if (tampilkanNama) {
                    Text(
                        transaksi.namaBarang,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    (if (masuk) "Barang masuk" else "Barang keluar") + " • " + formatWaktu(transaksi.waktu),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
                if (transaksi.catatan.isNotBlank()) {
                    Text(transaksi.catatan, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                (if (masuk) "+" else "−") + formatAngka(transaksi.jumlah),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (masuk) WarnaMasuk else WarnaKeluar,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            if (onHapus != null) {
                IconButton(onClick = onHapus) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus catatan", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TeksKosong(teks: String) {
    Text(
        teks,
        style = MaterialTheme.typography.bodyLarge,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
    )
}
