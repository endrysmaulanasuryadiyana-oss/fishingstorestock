package com.stokpancing.pelampung.data

/** Satu jenis barang, misalnya "Pelampung Bulu 3 gram". Harga dalam rupiah. */
data class Barang(
    val id: Long,
    val nama: String,
    val keterangan: String,
    val hargaBeli: Long,
    val hargaJual: Long,
    val stokMinimum: Int,
    val totalMasuk: Int,
    val totalKeluar: Int,
) {
    /** Jumlah barang yang ada di gudang sekarang. */
    val sisa: Int get() = totalMasuk - totalKeluar

    /** True jika stok sudah mencapai atau di bawah batas minimum. */
    val menipis: Boolean get() = sisa <= stokMinimum

    /** Nilai stok di gudang menurut harga jual. */
    val nilaiJual: Long get() = hargaJual * sisa
}

enum class JenisTransaksi { MASUK, KELUAR }

/** Catatan satu kali barang masuk atau keluar. notaId terisi jika berasal dari nota. */
data class Transaksi(
    val id: Long,
    val barangId: Long,
    val namaBarang: String,
    val jenis: JenisTransaksi,
    val jumlah: Int,
    val catatan: String,
    val waktu: Long,
    val notaId: Long?,
)

data class NotaItem(
    val namaBarang: String,
    val harga: Long,
    val jumlah: Int,
) {
    val subtotal: Long get() = harga * jumlah
}

data class Nota(
    val id: Long,
    val nomor: String,
    val pelanggan: String,
    val catatan: String,
    val waktu: Long,
    val items: List<NotaItem>,
) {
    val total: Long get() = items.sumOf { it.subtotal }
    val jumlahBarang: Int get() = items.sumOf { it.jumlah }
}

/** Barang yang dipilih saat membuat nota baru. */
data class ItemNotaBaru(val barang: Barang, val jumlah: Int)

/** Data toko yang tampil di kop nota. */
data class TokoInfo(
    val nama: String = "",
    val alamat: String = "",
    val telepon: String = "",
    val website: String = "",
    val catatanKaki: String = "Terima kasih atas kunjungan Anda",
)
