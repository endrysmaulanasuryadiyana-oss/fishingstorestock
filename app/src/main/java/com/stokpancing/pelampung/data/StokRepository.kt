package com.stokpancing.pelampung.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Database SQLite yang tersimpan di HP (tidak perlu internet). */
private class StokDatabase(context: Context) :
    SQLiteOpenHelper(context, "stok_pelampung.db", null, 2) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE barang (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                keterangan TEXT NOT NULL DEFAULT '',
                stok_minimum INTEGER NOT NULL DEFAULT 0,
                dibuat INTEGER NOT NULL,
                harga_beli INTEGER NOT NULL DEFAULT 0,
                harga_jual INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        buatTabelNota(db)
        db.execSQL(
            """
            CREATE TABLE transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                barang_id INTEGER NOT NULL REFERENCES barang(id) ON DELETE CASCADE,
                jenis TEXT NOT NULL,
                jumlah INTEGER NOT NULL,
                catatan TEXT NOT NULL DEFAULT '',
                waktu INTEGER NOT NULL,
                nota_id INTEGER REFERENCES nota(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_transaksi_barang ON transaksi(barang_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Versi 1 -> 2: tambah harga dan nota. Data lama tetap ada.
            db.execSQL("ALTER TABLE barang ADD COLUMN harga_beli INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE barang ADD COLUMN harga_jual INTEGER NOT NULL DEFAULT 0")
            buatTabelNota(db)
            db.execSQL("ALTER TABLE transaksi ADD COLUMN nota_id INTEGER REFERENCES nota(id) ON DELETE CASCADE")
        }
    }

    private fun buatTabelNota(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE nota (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nomor TEXT NOT NULL,
                pelanggan TEXT NOT NULL DEFAULT '',
                catatan TEXT NOT NULL DEFAULT '',
                waktu INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE nota_item (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nota_id INTEGER NOT NULL REFERENCES nota(id) ON DELETE CASCADE,
                barang_id INTEGER REFERENCES barang(id) ON DELETE SET NULL,
                nama_barang TEXT NOT NULL,
                harga INTEGER NOT NULL,
                jumlah INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

class StokRepository(context: Context) {

    private val helper = StokDatabase(context.applicationContext)

    suspend fun semuaBarang(): List<Barang> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT b.id, b.nama, b.keterangan, b.harga_beli, b.harga_jual, b.stok_minimum,
                   COALESCE(SUM(CASE WHEN t.jenis = 'MASUK'  THEN t.jumlah END), 0),
                   COALESCE(SUM(CASE WHEN t.jenis = 'KELUAR' THEN t.jumlah END), 0)
            FROM barang b
            LEFT JOIN transaksi t ON t.barang_id = b.id
            GROUP BY b.id
            ORDER BY b.nama COLLATE NOCASE
        """.trimIndent()
        helper.readableDatabase.rawQuery(sql, null).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        Barang(
                            id = c.getLong(0),
                            nama = c.getString(1),
                            keterangan = c.getString(2),
                            hargaBeli = c.getLong(3),
                            hargaJual = c.getLong(4),
                            stokMinimum = c.getInt(5),
                            totalMasuk = c.getInt(6),
                            totalKeluar = c.getInt(7),
                        )
                    )
                }
            }
        }
    }

    suspend fun semuaTransaksi(): List<Transaksi> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT t.id, t.barang_id, b.nama, t.jenis, t.jumlah, t.catatan, t.waktu, t.nota_id
            FROM transaksi t
            JOIN barang b ON b.id = t.barang_id
            ORDER BY t.waktu DESC, t.id DESC
        """.trimIndent()
        helper.readableDatabase.rawQuery(sql, null).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        Transaksi(
                            id = c.getLong(0),
                            barangId = c.getLong(1),
                            namaBarang = c.getString(2),
                            jenis = JenisTransaksi.valueOf(c.getString(3)),
                            jumlah = c.getInt(4),
                            catatan = c.getString(5),
                            waktu = c.getLong(6),
                            notaId = if (c.isNull(7)) null else c.getLong(7),
                        )
                    )
                }
            }
        }
    }

    suspend fun semuaNota(): List<Nota> = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        val itemPerNota = HashMap<Long, MutableList<NotaItem>>()
        db.rawQuery("SELECT nota_id, nama_barang, harga, jumlah FROM nota_item ORDER BY id", null).use { c ->
            while (c.moveToNext()) {
                itemPerNota.getOrPut(c.getLong(0)) { mutableListOf() }
                    .add(NotaItem(c.getString(1), c.getLong(2), c.getInt(3)))
            }
        }
        db.rawQuery(
            "SELECT id, nomor, pelanggan, catatan, waktu FROM nota ORDER BY waktu DESC, id DESC",
            null,
        ).use { c ->
            buildList {
                while (c.moveToNext()) {
                    val id = c.getLong(0)
                    add(
                        Nota(
                            id = id,
                            nomor = c.getString(1),
                            pelanggan = c.getString(2),
                            catatan = c.getString(3),
                            waktu = c.getLong(4),
                            items = itemPerNota[id].orEmpty(),
                        )
                    )
                }
            }
        }
    }

    suspend fun tambahBarang(
        nama: String,
        keterangan: String,
        hargaBeli: Long,
        hargaJual: Long,
        stokMinimum: Int,
        stokAwal: Int,
    ): Long = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val nilai = ContentValues().apply {
                put("nama", nama)
                put("keterangan", keterangan)
                put("harga_beli", hargaBeli)
                put("harga_jual", hargaJual)
                put("stok_minimum", stokMinimum)
                put("dibuat", System.currentTimeMillis())
            }
            val id = db.insertOrThrow("barang", null, nilai)
            if (stokAwal > 0) {
                simpanTransaksi(db, id, JenisTransaksi.MASUK, stokAwal, "Stok awal", null)
            }
            db.setTransactionSuccessful()
            id
        } finally {
            db.endTransaction()
        }
    }

    suspend fun ubahBarang(
        id: Long,
        nama: String,
        keterangan: String,
        hargaBeli: Long,
        hargaJual: Long,
        stokMinimum: Int,
    ) = withContext(Dispatchers.IO) {
        val nilai = ContentValues().apply {
            put("nama", nama)
            put("keterangan", keterangan)
            put("harga_beli", hargaBeli)
            put("harga_jual", hargaJual)
            put("stok_minimum", stokMinimum)
        }
        helper.writableDatabase.update("barang", nilai, "id = ?", arrayOf(id.toString()))
    }

    /** Menghapus barang beserta seluruh riwayatnya. Nota lama tetap tersimpan. */
    suspend fun hapusBarang(id: Long) = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete("barang", "id = ?", arrayOf(id.toString()))
    }

    suspend fun tambahTransaksi(
        barangId: Long,
        jenis: JenisTransaksi,
        jumlah: Int,
        catatan: String,
    ) = withContext(Dispatchers.IO) {
        simpanTransaksi(helper.writableDatabase, barangId, jenis, jumlah, catatan, null)
    }

    suspend fun hapusTransaksi(id: Long) = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete("transaksi", "id = ?", arrayOf(id.toString()))
    }

    /**
     * Membuat nota penjualan: menyimpan nota + rinciannya, lalu mencatat barang keluar.
     * Mengembalikan id nota baru.
     */
    suspend fun buatNota(
        pelanggan: String,
        catatan: String,
        items: List<ItemNotaBaru>,
    ): Long = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        val sekarang = System.currentTimeMillis()
        db.beginTransaction()
        try {
            val nomor = nomorNotaBerikutnya(db, sekarang)
            val notaId = db.insertOrThrow("nota", null, ContentValues().apply {
                put("nomor", nomor)
                put("pelanggan", pelanggan)
                put("catatan", catatan)
                put("waktu", sekarang)
            })
            for (item in items) {
                db.insertOrThrow("nota_item", null, ContentValues().apply {
                    put("nota_id", notaId)
                    put("barang_id", item.barang.id)
                    put("nama_barang", item.barang.nama)
                    put("harga", item.barang.hargaJual)
                    put("jumlah", item.jumlah)
                })
                val ket = if (pelanggan.isBlank()) "Nota $nomor" else "Nota $nomor – $pelanggan"
                simpanTransaksi(db, item.barang.id, JenisTransaksi.KELUAR, item.jumlah, ket, notaId)
            }
            db.setTransactionSuccessful()
            notaId
        } finally {
            db.endTransaction()
        }
    }

    /** Menghapus nota. Catatan barang keluarnya ikut terhapus, sehingga stok kembali. */
    suspend fun hapusNota(id: Long) = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete("nota", "id = ?", arrayOf(id.toString()))
    }

    /** Format nomor: NT-20261103-001 (urut per hari). */
    private fun nomorNotaBerikutnya(db: SQLiteDatabase, waktu: Long): String {
        val awalan = "NT-" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(waktu)) + "-"
        var urut = 1
        db.rawQuery(
            "SELECT nomor FROM nota WHERE nomor LIKE ? ORDER BY nomor DESC LIMIT 1",
            arrayOf("$awalan%"),
        ).use { c ->
            if (c.moveToFirst()) {
                urut = (c.getString(0).removePrefix(awalan).toIntOrNull() ?: 0) + 1
            }
        }
        return awalan + urut.toString().padStart(3, '0')
    }

    private fun simpanTransaksi(
        db: SQLiteDatabase,
        barangId: Long,
        jenis: JenisTransaksi,
        jumlah: Int,
        catatan: String,
        notaId: Long?,
    ): Long {
        val nilai = ContentValues().apply {
            put("barang_id", barangId)
            put("jenis", jenis.name)
            put("jumlah", jumlah)
            put("catatan", catatan)
            put("waktu", System.currentTimeMillis())
            if (notaId != null) put("nota_id", notaId)
        }
        return db.insertOrThrow("transaksi", null, nilai)
    }
}
