package com.baim.catatankontrak.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Warna baris berkas. Hanya bisa selain BASE jika berkas sudah dicentang. */
enum class DocColor {
    BASE, GREEN, YELLOW;

    /** Urutan siklus: Base -> Hijau -> Kuning -> Base */
    fun next(): DocColor = when (this) {
        BASE -> GREEN
        GREEN -> YELLOW
        YELLOW -> BASE
    }
}

data class DocItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val checked: Boolean = false,
    val color: DocColor = DocColor.BASE,
    val custom: Boolean = false,
) {
    /** Warna yang benar-benar ditampilkan (terkunci ke BASE jika belum dicentang). */
    val effectiveColor: DocColor get() = if (checked) color else DocColor.BASE

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("checked", checked)
        .put("color", color.name)
        .put("custom", custom)

    companion object {
        fun fromJson(o: JSONObject) = DocItem(
            id = o.optString("id", UUID.randomUUID().toString()).ifBlank { UUID.randomUUID().toString() },
            name = o.optString("name"),
            checked = o.optBoolean("checked"),
            color = runCatching { DocColor.valueOf(o.optString("color")) }
                .getOrDefault(DocColor.BASE),
            custom = o.optBoolean("custom"),
        )
    }
}

data class Contract(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val items: List<DocItem> = DefaultDocs.create(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val checkedCount: Int get() = items.count { it.checked }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("notes", notes)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)
        .put("items", JSONArray().apply { items.forEach { put(it.toJson()) } })

    companion object {
        fun fromJson(o: JSONObject): Contract {
            val arr = o.optJSONArray("items") ?: JSONArray()
            val parsedItems = (0 until arr.length()).mapNotNull {
                runCatching { DocItem.fromJson(arr.getJSONObject(it)) }.getOrNull()
            }
            return Contract(
                id = o.optString("id", UUID.randomUUID().toString()).ifBlank { UUID.randomUUID().toString() },
                title = o.optString("title"),
                notes = o.optString("notes"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                items = if (parsedItems.isNotEmpty()) parsedItems else DefaultDocs.create(),
            )
        }
    }
}

/** Urutan berkas kontrak default. */
object DefaultDocs {
    val names = listOf(
        "TOR",
        "RAB",
        "Justifikasi",
        "Nota Dinas",
        "SPK / Kontrak",
        "RAB Baru",
        "Amandemen",
        "BAPP",
        "BASTP",
        "BAP",
        "Nilai Kinerja",
        "Rekap Bobot",
        "Faktur Pajak",
        "Permohonan Pembayaran",
        "Kwitansi",
    )

    fun create(): List<DocItem> = names.map { DocItem(name = it) }
}
