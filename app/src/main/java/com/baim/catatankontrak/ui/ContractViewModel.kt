package com.baim.catatankontrak.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baim.catatankontrak.data.Contract
import com.baim.catatankontrak.data.ContractStore
import com.baim.catatankontrak.data.DocColor
import com.baim.catatankontrak.data.DocItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContractViewModel(app: Application) : AndroidViewModel(app) {

    private val store = ContractStore(app)

    private val _contracts = MutableStateFlow<List<Contract>>(emptyList())
    /** Selalu terurut: yang terakhir diubah berada paling atas. */
    val contracts: StateFlow<List<Contract>> = _contracts.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    /** Antrian simpan; CONFLATED = hanya snapshot terbaru yang ditulis. */
    private val saveQueue = Channel<List<Contract>>(Channel.CONFLATED)

    /**
     * Scope khusus penyimpanan, sengaja TIDAK memakai viewModelScope supaya
     * penulisan terakhir tidak ikut dibatalkan saat aplikasi ditutup.
     */
    private val saveScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { store.load() }
            _contracts.value = data.sortedByDescending { it.updatedAt }
            _loaded.value = true
        }
        saveScope.launch {
            for (snapshot in saveQueue) {
                runCatching { store.save(snapshot) }
            }
        }
    }

    override fun onCleared() {
        // Selesaikan sisa antrian lalu hentikan loop penyimpanan.
        saveQueue.close()
        super.onCleared()
    }

    private fun commit(newList: List<Contract>) {
        // Jangan menyimpan sebelum data lama selesai dibaca,
        // kalau tidak data lama bisa tertimpa list kosong.
        if (!_loaded.value) return
        val sorted = newList.sortedByDescending { it.updatedAt }
        _contracts.value = sorted
        if (saveQueue.trySend(sorted).isFailure) {
            // Antrian sudah ditutup (ViewModel dibersihkan) -> simpan langsung.
            saveScope.launch { runCatching { store.save(sorted) } }
        }
    }

    private fun modify(contractId: String, block: (Contract) -> Contract) {
        val now = System.currentTimeMillis()
        commit(_contracts.value.map {
            if (it.id == contractId) block(it).copy(updatedAt = now) else it
        })
    }

    private fun modifyItem(contractId: String, itemId: String, block: (DocItem) -> DocItem) =
        modify(contractId) { c ->
            c.copy(items = c.items.map { if (it.id == itemId) block(it) else it })
        }

    // ---------- Kontrak ----------

    fun addContract(title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        commit(listOf(Contract(title = t)) + _contracts.value)
    }

    fun renameContract(id: String, title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        modify(id) { it.copy(title = t) }
    }

    fun deleteContract(id: String) {
        commit(_contracts.value.filterNot { it.id == id })
    }

    /** Simpan catatan; diabaikan jika isinya sama (agar urutan kartu tidak berubah tanpa alasan). */
    fun updateNotes(id: String, notes: String) {
        val current = _contracts.value.firstOrNull { it.id == id } ?: return
        if (current.notes == notes) return
        modify(id) { it.copy(notes = notes) }
    }

    // ---------- Berkas ----------

    /** Centang / hapus centang. Saat di-uncheck, warna langsung dikunci kembali ke BASE. */
    fun toggleChecked(contractId: String, itemId: String) = modifyItem(contractId, itemId) {
        if (it.checked) it.copy(checked = false, color = DocColor.BASE)
        else it.copy(checked = true)
    }

    /** Ganti warna Base -> Hijau -> Kuning -> Base. Diabaikan jika belum dicentang. */
    fun cycleColor(contractId: String, itemId: String) {
        val item = _contracts.value.firstOrNull { it.id == contractId }
            ?.items?.firstOrNull { it.id == itemId } ?: return
        if (!item.checked) return
        modifyItem(contractId, itemId) { it.copy(color = it.color.next()) }
    }

    fun addItem(contractId: String, name: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        modify(contractId) { it.copy(items = it.items + DocItem(name = n, custom = true)) }
    }

    fun deleteItem(contractId: String, itemId: String) = modify(contractId) { c ->
        c.copy(items = c.items.filterNot { it.id == itemId })
    }
}
