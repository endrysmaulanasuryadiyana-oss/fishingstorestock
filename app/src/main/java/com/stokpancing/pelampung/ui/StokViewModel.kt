package com.stokpancing.pelampung.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stokpancing.pelampung.data.Barang
import com.stokpancing.pelampung.data.ItemNotaBaru
import com.stokpancing.pelampung.data.JenisTransaksi
import com.stokpancing.pelampung.data.Nota
import com.stokpancing.pelampung.data.StokRepository
import com.stokpancing.pelampung.data.TokoInfo
import com.stokpancing.pelampung.data.TokoRepository
import com.stokpancing.pelampung.data.Transaksi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val barang: List<Barang> = emptyList(),
    val riwayat: List<Transaksi> = emptyList(),
    val nota: List<Nota> = emptyList(),
    val toko: TokoInfo = TokoInfo(),
    val logo: Bitmap? = null,
    val sudahDimuat: Boolean = false,
)

/** Isian form tambah/ubah barang. */
data class FormBarang(
    val nama: String,
    val keterangan: String,
    val hargaBeli: Long,
    val hargaJual: Long,
    val stokMinimum: Int,
    val stokAwal: Int,
)

class StokViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = StokRepository(app)
    private val tokoRepo = TokoRepository(app)

    private val _state = MutableStateFlow(UiState(toko = tokoRepo.baca()))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _pesan = Channel<String>(Channel.BUFFERED)
    /** Pesan singkat yang ditampilkan di bagian bawah layar. */
    val pesan: Flow<String> = _pesan.receiveAsFlow()

    init {
        viewModelScope.launch {
            val logo = runCatching { tokoRepo.bacaLogo() }.getOrNull()
            _state.update { it.copy(logo = logo) }
            muatUlang()
        }
    }

    private suspend fun muatUlang() {
        val barang = repo.semuaBarang()
        val riwayat = repo.semuaTransaksi()
        val nota = repo.semuaNota()
        _state.update { it.copy(barang = barang, riwayat = riwayat, nota = nota, sudahDimuat = true) }
    }

    private fun jalankan(pesanSelesai: String, aksi: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                aksi()
                muatUlang()
                _pesan.send(pesanSelesai)
            } catch (e: Exception) {
                _pesan.send("Gagal menyimpan: ${e.message}")
            }
        }
    }

    // ---------- Barang ----------

    fun tambahBarang(f: FormBarang) = jalankan("Barang \"${f.nama}\" ditambahkan") {
        repo.tambahBarang(f.nama, f.keterangan, f.hargaBeli, f.hargaJual, f.stokMinimum, f.stokAwal)
    }

    fun ubahBarang(id: Long, f: FormBarang) = jalankan("Data barang diperbarui") {
        repo.ubahBarang(id, f.nama, f.keterangan, f.hargaBeli, f.hargaJual, f.stokMinimum)
    }

    fun hapusBarang(barang: Barang) =
        jalankan("Barang \"${barang.nama}\" dihapus") { repo.hapusBarang(barang.id) }

    fun tambahTransaksi(barang: Barang, jenis: JenisTransaksi, jumlah: Int, catatan: String) {
        if (jumlah <= 0) return
        if (jenis == JenisTransaksi.KELUAR && jumlah > barang.sisa) return
        val teks = if (jenis == JenisTransaksi.MASUK) {
            "$jumlah barang masuk dicatat"
        } else {
            "$jumlah barang keluar dicatat"
        }
        jalankan(teks) { repo.tambahTransaksi(barang.id, jenis, jumlah, catatan) }
    }

    fun hapusTransaksi(transaksi: Transaksi) {
        if (transaksi.notaId != null) return // dihapus lewat menu Nota
        val barang = _state.value.barang.find { it.id == transaksi.barangId } ?: return
        if (transaksi.jenis == JenisTransaksi.MASUK && barang.sisa - transaksi.jumlah < 0) {
            viewModelScope.launch {
                _pesan.send("Tidak bisa dihapus: stok di gudang akan menjadi minus")
            }
            return
        }
        jalankan("Catatan dihapus") { repo.hapusTransaksi(transaksi.id) }
    }

    // ---------- Nota ----------

    fun buatNota(
        pelanggan: String,
        catatan: String,
        items: List<ItemNotaBaru>,
        onSelesai: (Long) -> Unit,
    ) {
        val stokTerbaru = _state.value.barang.associateBy { it.id }
        val valid = items.filter { it.jumlah > 0 }
        if (valid.isEmpty()) return
        val kurang = valid.firstOrNull { it.jumlah > (stokTerbaru[it.barang.id]?.sisa ?: 0) }
        viewModelScope.launch {
            if (kurang != null) {
                _pesan.send("Stok \"${kurang.barang.nama}\" tidak cukup")
                return@launch
            }
            try {
                val id = repo.buatNota(pelanggan, catatan, valid)
                muatUlang()
                onSelesai(id)
                _pesan.send("Nota tersimpan, stok sudah dikurangi")
            } catch (e: Exception) {
                _pesan.send("Gagal menyimpan nota: ${e.message}")
            }
        }
    }

    fun hapusNota(nota: Nota) =
        jalankan("Nota ${nota.nomor} dihapus, stok dikembalikan") { repo.hapusNota(nota.id) }

    // ---------- Toko ----------

    fun simpanToko(info: TokoInfo) {
        tokoRepo.simpan(info)
        _state.update { it.copy(toko = info) }
        viewModelScope.launch { _pesan.send("Profil toko disimpan") }
    }

    fun simpanLogo(uri: Uri) {
        viewModelScope.launch {
            try {
                val logo = tokoRepo.simpanLogo(uri)
                _state.update { it.copy(logo = logo) }
                _pesan.send("Logo toko diperbarui")
            } catch (e: Exception) {
                _pesan.send("Gagal memuat gambar: ${e.message}")
            }
        }
    }

    fun hapusLogo() {
        viewModelScope.launch {
            tokoRepo.hapusLogo()
            _state.update { it.copy(logo = null) }
            _pesan.send("Logo dihapus")
        }
    }

    /** Teks laporan stok untuk dibagikan lewat WhatsApp, dll. */
    fun laporanTeks(): String {
        val s = _state.value
        val daftar = s.barang
        if (daftar.isEmpty()) return "Belum ada data barang."
        return buildString {
            appendLine("LAPORAN STOK ${s.toko.nama.ifBlank { "PELAMPUNG PANCING" }.uppercase()}")
            appendLine(formatWaktu(System.currentTimeMillis()))
            appendLine()
            daftar.forEachIndexed { i, b ->
                appendLine("${i + 1}. ${b.nama} @ ${formatRupiah(b.hargaJual)}")
                appendLine(
                    "   Masuk: ${formatAngka(b.totalMasuk)} | Keluar: ${formatAngka(b.totalKeluar)}" +
                        " | Di gudang: ${formatAngka(b.sisa)}" +
                        if (b.menipis) "  (STOK MENIPIS)" else ""
                )
            }
            appendLine()
            appendLine("Total di gudang: ${formatAngka(daftar.sumOf { it.sisa })} pcs")
            append("Nilai stok (harga jual): ${formatRupiah(daftar.sumOf { it.nilaiJual })}")
        }
    }
}
