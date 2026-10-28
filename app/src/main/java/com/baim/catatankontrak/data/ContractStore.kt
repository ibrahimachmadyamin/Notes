package com.baim.catatankontrak.data

import android.content.Context
import org.json.JSONArray
import java.io.File

/**
 * Penyimpanan lokal sederhana berbasis file JSON di internal storage.
 * Penulisan dilakukan secara atomik (tulis ke file sementara lalu rename)
 * agar data tidak korup jika aplikasi tertutup di tengah penyimpanan.
 */
class ContractStore(context: Context) {
    private val dir = context.filesDir
    private val file = File(dir, "contracts.json")
    private val tmp = File(dir, "contracts.json.tmp")

    /**
     * Membaca data. Urutan: file utama -> file sementara (sisa simpan yang terputus).
     * Jika file utama rusak, file tersebut dicadangkan (tidak ditimpa) agar data
     * masih bisa diselamatkan secara manual.
     */
    @Synchronized
    fun load(): List<Contract> {
        parse(file)?.let { return it }
        if (file.exists()) {
            // File utama ada tapi rusak -> cadangkan sebelum suatu saat tertimpa.
            file.renameTo(File(dir, "contracts.corrupt.${System.currentTimeMillis()}.json"))
        }
        parse(tmp)?.let { return it }
        return emptyList()
    }

    private fun parse(f: File): List<Contract>? {
        if (!f.exists()) return null
        return runCatching {
            val arr = JSONArray(f.readText())
            (0 until arr.length()).mapNotNull { i ->
                runCatching { Contract.fromJson(arr.getJSONObject(i)) }.getOrNull()
            }
        }.getOrNull()
    }

    @Synchronized
    fun save(contracts: List<Contract>) {
        runCatching {
            val json = JSONArray().apply { contracts.forEach { put(it.toJson()) } }.toString()
            tmp.writeText(json)
            // Di Android (Linux), renameTo menimpa file tujuan secara atomik.
            // JANGAN hapus file utama dulu: kalau app mati di antaranya, data hilang.
            if (!tmp.renameTo(file)) {
                file.writeText(json)
                tmp.delete()
            }
        }
    }
}
