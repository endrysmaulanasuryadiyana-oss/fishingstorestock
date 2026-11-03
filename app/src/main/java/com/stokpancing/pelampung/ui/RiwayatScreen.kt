package com.stokpancing.pelampung.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stokpancing.pelampung.data.JenisTransaksi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatKonten(state: UiState, padding: PaddingValues) {
    // 0 = semua, 1 = masuk, 2 = keluar
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val tampil = remember(state.riwayat, filter) {
        when (filter) {
            1 -> state.riwayat.filter { it.jenis == JenisTransaksi.MASUK }
            2 -> state.riwayat.filter { it.jenis == JenisTransaksi.KELUAR }
            else -> state.riwayat
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Semua", "Masuk", "Keluar").forEachIndexed { i, label ->
                    FilterChip(
                        selected = filter == i,
                        onClick = { filter = i },
                        label = { Text(label) },
                    )
                }
            }
        }
        if (state.sudahDimuat && tampil.isEmpty()) {
            item { TeksKosong("Belum ada catatan barang masuk atau keluar.") }
        }
        items(tampil, key = { it.id }) { t ->
            BarisTransaksi(t, tampilkanNama = true)
        }
    }
}
