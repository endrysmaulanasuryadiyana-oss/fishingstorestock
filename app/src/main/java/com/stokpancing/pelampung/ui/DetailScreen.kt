package com.stokpancing.pelampung.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stokpancing.pelampung.data.Barang
import com.stokpancing.pelampung.data.JenisTransaksi
import com.stokpancing.pelampung.data.Transaksi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    barang: Barang?,
    riwayat: List<Transaksi>,
    semuaNama: List<String>,
    sudahDimuat: Boolean,
    vm: StokViewModel,
    snackbar: SnackbarHostState,
    onKembali: () -> Unit,
) {
    // Barang sudah dihapus -> kembali ke daftar.
    if (barang == null) {
        LaunchedEffect(sudahDimuat) { if (sudahDimuat) onKembali() }
        return
    }

    var dialogEdit by remember { mutableStateOf(false) }
    var dialogHapus by remember { mutableStateOf(false) }
    var jenisDialog by remember { mutableStateOf<JenisTransaksi?>(null) }
    var transaksiDihapus by remember { mutableStateOf<Transaksi?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(barang.nama, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onKembali) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { dialogEdit = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Ubah barang")
                    }
                    IconButton(onClick = { dialogHapus = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus barang")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Column {
                KartuStok(barang)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { jenisDialog = JenisTransaksi.MASUK },
                        colors = ButtonDefaults.buttonColors(containerColor = WarnaMasuk),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Barang Masuk")
                    }
                    OutlinedButton(
                        onClick = { jenisDialog = JenisTransaksi.KELUAR },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarnaKeluar),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Text("−  Barang Keluar")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Riwayat barang ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            } }
            if (riwayat.isEmpty()) {
                item { TeksKosong("Belum ada catatan masuk/keluar untuk barang ini.") }
            }
            items(riwayat, key = { it.id }) { t ->
                BarisTransaksi(
                    t,
                    tampilkanNama = false,
                    // Catatan dari nota dihapus lewat menu Nota agar nota tetap sesuai.
                    onHapus = if (t.notaId == null) {
                        { transaksiDihapus = t }
                    } else {
                        null
                    },
                )
            }
        }
    }

    if (dialogEdit) {
        DialogBarang(
            awal = barang,
            namaTerpakai = semuaNama,
            onTutup = { dialogEdit = false },
            onSimpan = { form ->
                vm.ubahBarang(barang.id, form)
                dialogEdit = false
            },
        )
    }

    jenisDialog?.let { jenis ->
        DialogTransaksi(
            barang = barang,
            jenis = jenis,
            onTutup = { jenisDialog = null },
            onSimpan = { jumlah, catatan ->
                vm.tambahTransaksi(barang, jenis, jumlah, catatan)
                jenisDialog = null
            },
        )
    }

    if (dialogHapus) {
        DialogKonfirmasi(
            judul = "Hapus barang?",
            isi = "\"${barang.nama}\" beserta seluruh riwayat masuk/keluarnya akan dihapus permanen.",
            tombol = "Hapus",
            onYa = {
                dialogHapus = false
                vm.hapusBarang(barang)
            },
            onTutup = { dialogHapus = false },
        )
    }

    transaksiDihapus?.let { t ->
        val jenis = if (t.jenis == JenisTransaksi.MASUK) "barang masuk" else "barang keluar"
        DialogKonfirmasi(
            judul = "Hapus catatan?",
            isi = "Catatan $jenis sebanyak ${formatAngka(t.jumlah)} (${formatWaktu(t.waktu)}) akan dihapus " +
                "dan stok di gudang dihitung ulang.",
            tombol = "Hapus",
            onYa = {
                transaksiDihapus = null
                vm.hapusTransaksi(t)
            },
            onTutup = { transaksiDihapus = null },
        )
    }
}

@Composable
private fun KartuStok(barang: Barang) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Jumlah di gudang", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Text(
                        formatAngka(barang.sisa),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (barang.menipis) WarnaMenipis else MaterialTheme.colorScheme.primary,
                    )
                    if (barang.menipis) LabelMenipis()
                }
            }
            if (barang.keterangan.isNotBlank()) {
                Text(
                    barang.keterangan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Row(Modifier.fillMaxWidth()) {
                KolomAngka("Stok Masuk", barang.totalMasuk, WarnaMasuk, Modifier.weight(1f))
                KolomAngka("Barang Keluar", barang.totalKeluar, WarnaKeluar, Modifier.weight(1f))
                KolomAngka("Batas Menipis", barang.stokMinimum, Color.Gray, Modifier.weight(1f))
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            BarisHarga("Harga jual", formatRupiah(barang.hargaJual), MaterialTheme.colorScheme.secondary)
            BarisHarga("Harga beli / modal", formatRupiah(barang.hargaBeli), Color.Gray)
            BarisHarga("Nilai stok di gudang", formatRupiah(barang.nilaiJual), MaterialTheme.colorScheme.primary)
            if (barang.hargaBeli > 0 && barang.hargaJual > 0) {
                BarisHarga("Untung per pcs", formatRupiah(barang.hargaJual - barang.hargaBeli), WarnaMasuk)
            }
        }
    }
}

@Composable
private fun BarisHarga(label: String, nilai: String, warna: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(nilai, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = warna)
    }
}
