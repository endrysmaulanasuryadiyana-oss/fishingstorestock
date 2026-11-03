package com.stokpancing.pelampung.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BerandaKonten(
    state: UiState,
    padding: PaddingValues,
    onBukaBarang: (Long) -> Unit,
    onPilihLogo: () -> Unit,
    onAturToko: () -> Unit,
) {
    var cari by rememberSaveable { mutableStateOf("") }
    val tampil = remember(state.barang, cari) {
        state.barang.filter {
            it.nama.contains(cari.trim(), ignoreCase = true) ||
                it.keterangan.contains(cari.trim(), ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { KopToko(state.toko, state.logo, onPilihLogo, onAturToko) }

        item { Ringkasan(state) }

        if (state.barang.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = cari,
                    onValueChange = { cari = it },
                    placeholder = { Text("Cari nama barang…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        when {
            !state.sudahDimuat -> Unit
            state.barang.isEmpty() -> item {
                TeksKosong("Belum ada barang.\nTekan \"Tambah Barang\" untuk mulai mencatat stok pelampung.")
            }
            tampil.isEmpty() -> item { TeksKosong("Barang \"$cari\" tidak ditemukan.") }
            else -> items(tampil, key = { it.id }) { barang ->
                KartuBarang(barang) { onBukaBarang(barang.id) }
            }
        }
    }
}

@Composable
private fun Ringkasan(state: UiState) {
    val totalGudang = state.barang.sumOf { it.sisa }
    val menipis = state.barang.count { it.menipis }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Total barang di gudang",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                "${formatAngka(totalGudang)} pcs",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${state.barang.size} jenis barang",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    if (menipis == 0) "Semua stok aman" else "$menipis stok menipis",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (menipis == 0) WarnaMasuk else WarnaMenipis,
                )
            }
            Text(
                "Nilai stok (harga jual): ${formatRupiah(state.barang.sumOf { it.nilaiJual })}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
