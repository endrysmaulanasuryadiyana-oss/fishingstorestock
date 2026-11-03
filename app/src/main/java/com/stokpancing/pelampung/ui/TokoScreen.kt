package com.stokpancing.pelampung.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stokpancing.pelampung.data.TokoInfo

/** Menu Toko: logo dan data toko untuk kop nota. */
@Composable
fun TokoKonten(
    state: UiState,
    padding: PaddingValues,
    onPilihLogo: () -> Unit,
    onHapusLogo: () -> Unit,
    onSimpan: (TokoInfo) -> Unit,
) {
    val t = state.toko
    var nama by rememberSaveable(t) { mutableStateOf(t.nama) }
    var alamat by rememberSaveable(t) { mutableStateOf(t.alamat) }
    var telepon by rememberSaveable(t) { mutableStateOf(t.telepon) }
    var website by rememberSaveable(t) { mutableStateOf(t.website) }
    var catatanKaki by rememberSaveable(t) { mutableStateOf(t.catatanKaki) }

    val berubah = nama != t.nama || alamat != t.alamat || telepon != t.telepon ||
        website != t.website || catatanKaki != t.catatanKaki

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Logo Toko", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoToko(state.logo, 96.dp, onPilihLogo)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Button(onClick = onPilihLogo) {
                            Text(if (state.logo == null) "Pilih dari Galeri" else "Ganti Logo")
                        }
                        if (state.logo != null) {
                            TextButton(onClick = onHapusLogo) {
                                Text("Hapus Logo", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                Text(
                    "Logo tampil di atas daftar barang dan di kop nota.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Data Toko (untuk nota)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama toko") },
                    placeholder = { Text("cth. Toko Pancing Jaya") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = alamat,
                    onValueChange = { alamat = it },
                    label = { Text("Alamat") },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = telepon,
                    onValueChange = { telepon = it },
                    label = { Text("No. telepon / WhatsApp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website (opsional)") },
                    placeholder = { Text("cth. www.tokopancing.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = catatanKaki,
                    onValueChange = { catatanKaki = it },
                    label = { Text("Tulisan di bawah nota") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Button(
            enabled = berubah,
            onClick = {
                onSimpan(
                    TokoInfo(
                        nama = nama.trim(),
                        alamat = alamat.trim(),
                        telepon = telepon.trim(),
                        website = website.trim(),
                        catatanKaki = catatanKaki.trim(),
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) { Text("Simpan Profil Toko") }

        if (berubah) {
            OutlinedButton(
                onClick = {
                    nama = t.nama; alamat = t.alamat; telepon = t.telepon
                    website = t.website; catatanKaki = t.catatanKaki
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Batalkan perubahan") }
        }
    }
}
