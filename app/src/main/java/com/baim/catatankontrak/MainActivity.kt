package com.baim.catatankontrak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baim.catatankontrak.ui.ContractViewModel
import com.baim.catatankontrak.ui.DetailScreen
import com.baim.catatankontrak.ui.HomeScreen
import com.baim.catatankontrak.ui.theme.CatatanKontrakTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatatanKontrakTheme {
                val vm: ContractViewModel = viewModel()
                val contracts by vm.contracts.collectAsStateWithLifecycle()
                val loaded by vm.loaded.collectAsStateWithLifecycle()

                // null = halaman utama, selain itu = id kontrak yang dibuka
                var openId by rememberSaveable { mutableStateOf<String?>(null) }
                val opened = openId?.let { id -> contracts.firstOrNull { it.id == id } }

                // Jika kontrak yang dibuka sudah tidak ada, kembali ke halaman utama
                LaunchedEffect(loaded, opened, openId) {
                    if (loaded && openId != null && opened == null) openId = null
                }

                BackHandler(enabled = openId != null) { openId = null }

                AnimatedContent(
                    targetState = opened?.id,
                    transitionSpec = {
                        val forward = targetState != null
                        val dir = if (forward) 1 else -1
                        (slideInHorizontally(tween(260)) { it / 4 * dir } + fadeIn(tween(260)))
                            .togetherWith(slideOutHorizontally(tween(220)) { -it / 4 * dir } + fadeOut(tween(180)))
                    },
                    label = "nav",
                    modifier = Modifier.fillMaxSize(),
                ) { id ->
                    val c = id?.let { cid -> contracts.firstOrNull { it.id == cid } }
                    if (c == null) {
                        HomeScreen(
                            contracts = contracts,
                            loaded = loaded,
                            onOpen = { openId = it },
                            onAdd = vm::addContract,
                            onDelete = vm::deleteContract,
                        )
                    } else {
                        DetailScreen(
                            contract = c,
                            onBack = { openId = null },
                            onRename = { vm.renameContract(c.id, it) },
                            onToggleChecked = { vm.toggleChecked(c.id, it) },
                            onCycleColor = { vm.cycleColor(c.id, it) },
                            onAddItem = { vm.addItem(c.id, it) },
                            onDeleteItem = { vm.deleteItem(c.id, it) },
                            onNotesChange = { vm.updateNotes(c.id, it) },
                        )
                    }
                }
            }
        }
    }
}
