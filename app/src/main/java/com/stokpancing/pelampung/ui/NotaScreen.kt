package com.stokpancing.pelampung.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stokpancing.pelampung.data.Barang
import com.stokpancing.pelampung.data.ItemNotaBaru
import com.stokpancing.pelampung.data.Nota

// ======================= Daftar nota (tab) =======================

@Composable
fun NotaKonten(state: UiState, padding: PaddingValues, onBukaNota: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.nota.isNotEmpty()) {
            item {
                val hariIni = formatWaktu(System.currentTimeMillis()).substringBefore(",")
                val notaHariIni = state.nota.filter { formatWaktu(it.waktu).startsWith(hariIni) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Penjualan hari ini", style = MaterialTheme.typography.labelLarge)
                        Text(
                            formatRupiah(notaHariIni.sumOf { it.total }),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "${notaHariIni.size} nota • ${formatAngka(notaHariIni.sumOf { it.jumlahBarang })} pcs terjual",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
        if (state.sudahDimuat && state.nota.isEmpty()) {
            item {
                TeksKosong("Belum ada nota.\nTekan \"Buat Nota\" untuk mencatat penjualan. Stok akan berkurang otomatis.")
            }
        }
        items(state.nota, key = { it.id }) { nota ->
            KartuNota(nota) { onBukaNota(nota.id) }
        }
    }
}

@Composable
private fun KartuNota(nota: Nota, onKlik: () -> Unit) {
    Card(
        onClick = onKlik,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(nota.nomor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    nota.pelanggan.ifBlank { "Pelanggan umum" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${formatWaktu(nota.waktu)} • ${formatAngka(nota.jumlahBarang)} pcs",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            }
            Text(
                formatRupiah(nota.total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

// ======================= Buat nota =======================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuatNotaScreen(
    state: UiState,
    snackbar: SnackbarHostState,
    onKembali: () -> Unit,
    onSimpan: (pelanggan: String, catatan: String, items: List<ItemNotaBaru>) -> Unit,
) {
    val jumlah = remember { mutableStateMapOf<Long, Int>() }
    var pelanggan by rememberSaveable { mutableStateOf("") }
    var catatan by rememberSaveable { mutableStateOf("") }
    var cari by rememberSaveable { mutableStateOf("") }
    var menyimpan by remember { mutableStateOf(false) }
    // Cegah tombol simpan ditekan dua kali, lalu aktifkan lagi (misalnya jika gagal).
    LaunchedEffect(menyimpan) {
        if (menyimpan) {
            kotlinx.coroutines.delay(2000)
            menyimpan = false
        }
    }

    val dipilih = state.barang.mapNotNull { b ->
        val n = jumlah[b.id] ?: 0
        if (n > 0) ItemNotaBaru(b, n) else null
    }
    val total = dipilih.sumOf { it.barang.hargaJual * it.jumlah }
    val tampil = state.barang.filter { it.nama.contains(cari.trim(), ignoreCase = true) }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Buat Nota") },
                navigationIcon = {
                    IconButton(onClick = onKembali) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${dipilih.size} jenis • ${formatAngka(dipilih.sumOf { it.jumlah })} pcs",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                        Text(
                            formatRupiah(total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Button(
                        enabled = dipilih.isNotEmpty() && !menyimpan,
                        onClick = {
                            menyimpan = true
                            onSimpan(pelanggan.trim(), catatan.trim(), dipilih)
                        },
                    ) { Text("Simpan Nota") }
                }
            }
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
            item {
                OutlinedTextField(
                    value = pelanggan,
                    onValueChange = { pelanggan = it },
                    label = { Text("Nama pelanggan (opsional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = cari,
                    onValueChange = { cari = it },
                    placeholder = { Text("Cari barang…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (state.barang.isEmpty()) {
                item { TeksKosong("Belum ada barang. Tambahkan barang dulu di menu Stok.") }
            }
            items(tampil, key = { it.id }) { b ->
                BarisPilihBarang(
                    barang = b,
                    jumlah = jumlah[b.id] ?: 0,
                    onUbah = { n -> jumlah[b.id] = n.coerceIn(0, b.sisa) },
                )
            }
            item {
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan nota (opsional)") },
                    placeholder = { Text("cth. Lunas / transfer BCA") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun BarisPilihBarang(barang: Barang, jumlah: Int, onUbah: (Int) -> Unit) {
    val habis = barang.sisa <= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (jumlah > 0) MaterialTheme.colorScheme.primaryContainer else Color.White,
        ),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    barang.nama,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${formatRupiah(barang.hargaJual)} • Stok ${formatAngka(barang.sisa)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (habis) MaterialTheme.colorScheme.error else Color.Gray,
                )
                if (jumlah > 0) {
                    Text(
                        "Subtotal ${formatRupiah(barang.hargaJual * jumlah)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (habis) {
                Text("Stok habis", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
            } else {
                FilledTonalIconButton(onClick = { onUbah(jumlah - 1) }, enabled = jumlah > 0) {
                    Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = if (jumlah == 0) "" else jumlah.toString(),
                    onValueChange = { onUbah(hanyaAngka(it, 6).toIntOrNull() ?: 0) },
                    placeholder = { Text("0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(76.dp)
                        .padding(horizontal = 4.dp),
                )
                FilledTonalIconButton(onClick = { onUbah(jumlah + 1) }, enabled = jumlah < barang.sisa) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                }
            }
        }
    }
}

// ======================= Detail nota =======================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailNotaScreen(
    nota: Nota?,
    state: UiState,
    snackbar: SnackbarHostState,
    onKembali: () -> Unit,
    onHapus: (Nota) -> Unit,
) {
    if (nota == null) {
        LaunchedEffect(state.sudahDimuat) { if (state.sudahDimuat) onKembali() }
        return
    }
    val context = LocalContext.current
    val gambar = remember(nota, state.toko, state.logo) { NotaGambar.buat(nota, state.toko, state.logo) }
    val gambarCompose = remember(gambar) { gambar.asImageBitmap() }
    var konfirmasiHapus by remember { mutableStateOf(false) }
    val bagikan = { bagikanGambar(context, gambar, nota.nomor) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nota.nomor) },
                navigationIcon = {
                    IconButton(onClick = onKembali) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = bagikan) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan nota")
                    }
                    IconButton(onClick = { konfirmasiHapus = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus nota")
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            ) {
                Image(
                    bitmap = gambarCompose,
                    contentDescription = "Nota ${nota.nomor}",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = bagikan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Bagikan Nota (WhatsApp, dll.)")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Kop nota diambil dari menu Toko. Ubah nama, alamat, atau logo di sana.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
        }
    }

    if (konfirmasiHapus) {
        DialogKonfirmasi(
            judul = "Hapus nota?",
            isi = "Nota ${nota.nomor} akan dihapus dan stok barangnya dikembalikan ke gudang.",
            tombol = "Hapus",
            onYa = {
                konfirmasiHapus = false
                onHapus(nota)
            },
            onTutup = { konfirmasiHapus = false },
        )
    }
}
