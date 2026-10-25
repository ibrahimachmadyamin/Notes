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
    private val file = File(context.filesDir, "contracts.json")
    private val tmp = File(context.filesDir, "contracts.json.tmp")

    fun load(): List<Contract> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { Contract.fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun save(contracts: List<Contract>) {
        runCatching {
            val arr = JSONArray().apply { contracts.forEach { put(it.toJson()) } }
            tmp.writeText(arr.toString())
            if (file.exists()) {
                file.delete()
            }
            if (!tmp.renameTo(file)) {
                file.writeText(arr.toString())
                tmp.delete()
            }
        }
    }
}

