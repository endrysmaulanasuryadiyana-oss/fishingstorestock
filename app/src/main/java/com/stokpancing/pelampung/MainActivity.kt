package com.stokpancing.pelampung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stokpancing.pelampung.ui.BerandaKonten
import com.stokpancing.pelampung.ui.BuatNotaScreen
import com.stokpancing.pelampung.ui.DetailNotaScreen
import com.stokpancing.pelampung.ui.DetailScreen
import com.stokpancing.pelampung.ui.DialogBarang
import com.stokpancing.pelampung.ui.NotaKonten
import com.stokpancing.pelampung.ui.RiwayatKonten
import com.stokpancing.pelampung.ui.StokTheme
import com.stokpancing.pelampung.ui.StokViewModel
import com.stokpancing.pelampung.ui.TokoKonten
import com.stokpancing.pelampung.ui.bagikanTeks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StokTheme {
                AplikasiStok()
            }
        }
    }
}

private const val TIDAK_ADA = -1L

private const val TAB_STOK = 0
private const val TAB_NOTA = 1
private const val TAB_RIWAYAT = 2
private const val TAB_TOKO = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AplikasiStok(vm: StokViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    var tab by rememberSaveable { mutableIntStateOf(TAB_STOK) }
    var detailBarangId by rememberSaveable { mutableLongStateOf(TIDAK_ADA) }
    var detailNotaId by rememberSaveable { mutableLongStateOf(TIDAK_ADA) }
    var buatNota by rememberSaveable { mutableStateOf(false) }
    var dialogTambah by rememberSaveable { mutableStateOf(false) }

    // Pilih logo dari galeri (Photo Picker Android).
    val pemilihGambar = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.simpanLogo(uri)
    }
    val bukaGaleri: () -> Unit = {
        pemilihGambar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    LaunchedEffect(Unit) {
        vm.pesan.collect { snackbar.showSnackbar(it) }
    }

    when {
        buatNota -> {
            BackHandler { buatNota = false }
            BuatNotaScreen(
                state = state,
                snackbar = snackbar,
                onKembali = { buatNota = false },
                onSimpan = { pelanggan, catatan, items ->
                    vm.buatNota(pelanggan, catatan, items) { idBaru ->
                        buatNota = false
                        tab = TAB_NOTA
                        detailNotaId = idBaru
                    }
                },
            )
        }

        detailNotaId != TIDAK_ADA -> {
            BackHandler { detailNotaId = TIDAK_ADA }
            DetailNotaScreen(
                nota = state.nota.find { it.id == detailNotaId },
                state = state,
                snackbar = snackbar,
                onKembali = { detailNotaId = TIDAK_ADA },
                onHapus = { nota ->
                    detailNotaId = TIDAK_ADA
                    vm.hapusNota(nota)
                },
            )
        }

        detailBarangId != TIDAK_ADA -> {
            BackHandler { detailBarangId = TIDAK_ADA }
            DetailScreen(
                barang = state.barang.find { it.id == detailBarangId },
                riwayat = state.riwayat.filter { it.barangId == detailBarangId },
                semuaNama = state.barang.map { it.nama },
                sudahDimuat = state.sudahDimuat,
                vm = vm,
                snackbar = snackbar,
                onKembali = { detailBarangId = TIDAK_ADA },
            )
        }

        else -> Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (tab) {
                                TAB_STOK -> state.toko.nama.ifBlank { "Stok Pelampung Pancing" }
                                TAB_NOTA -> "Nota Penjualan"
                                TAB_RIWAYAT -> "Riwayat Masuk & Keluar"
                                else -> "Profil Toko"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    actions = {
                        if (tab == TAB_STOK) {
                            IconButton(onClick = { bagikanTeks(context, vm.laporanTeks()) }) {
                                Icon(Icons.Default.Share, contentDescription = "Bagikan laporan stok")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == TAB_STOK,
                        onClick = { tab = TAB_STOK },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Stok") },
                    )
                    NavigationBarItem(
                        selected = tab == TAB_NOTA,
                        onClick = { tab = TAB_NOTA },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                        label = { Text("Nota") },
                    )
                    NavigationBarItem(
                        selected = tab == TAB_RIWAYAT,
                        onClick = { tab = TAB_RIWAYAT },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        label = { Text("Riwayat") },
                    )
                    NavigationBarItem(
                        selected = tab == TAB_TOKO,
                        onClick = { tab = TAB_TOKO },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Toko") },
                    )
                }
            },
            floatingActionButton = {
                when (tab) {
                    TAB_STOK -> ExtendedFloatingActionButton(
                        onClick = { dialogTambah = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Tambah Barang") },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                    )
                    TAB_NOTA -> ExtendedFloatingActionButton(
                        onClick = { buatNota = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Buat Nota") },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                    )
                    else -> Unit
                }
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            when (tab) {
                TAB_STOK -> BerandaKonten(
                    state = state,
                    padding = padding,
                    onBukaBarang = { id -> detailBarangId = id },
                    onPilihLogo = bukaGaleri,
                    onAturToko = { tab = TAB_TOKO },
                )
                TAB_NOTA -> NotaKonten(state, padding) { id -> detailNotaId = id }
                TAB_RIWAYAT -> RiwayatKonten(state, padding)
                else -> TokoKonten(
                    state = state,
                    padding = padding,
                    onPilihLogo = bukaGaleri,
                    onHapusLogo = vm::hapusLogo,
                    onSimpan = vm::simpanToko,
                )
            }
        }
    }

    if (dialogTambah) {
        DialogBarang(
            awal = null,
            namaTerpakai = state.barang.map { it.nama },
            onTutup = { dialogTambah = false },
            onSimpan = { form ->
                vm.tambahBarang(form)
                dialogTambah = false
            },
        )
    }
}
