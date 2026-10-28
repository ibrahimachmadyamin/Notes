package com.baim.catatankontrak.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.baim.catatankontrak.data.Contract
import com.baim.catatankontrak.data.DocColor
import com.baim.catatankontrak.data.DocItem
import com.baim.catatankontrak.ui.theme.LocalDocPalette
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(

    contract: Contract,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onToggleChecked: (String) -> Unit,
    onCycleColor: (String) -> Unit,
    onAddItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onNotesChange: (String) -> Unit,
) {
    var showRename by remember { mutableStateOf(false) }
    var showAddItem by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<DocItem?>(null) }
    var notesFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val isKeyboardOpen = WindowInsets.isImeVisible

    // Saat keyboard ditutup (via tombol Back / tombol sembunyikan keyboard),
    // otomatis lepaskan fokus dari kolom catatan agar panel langsung turun kembali.
    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
            notesFocused = false
        }
    }

    // Default proporsi: 3/4 daftar berkas (list: 3f) dan 1/4 catatan (notes: 1f).
    // Hanya saat keyboard terbuka DAN catatan sedang diedit, catatan naik memberi ruang (list: 1.5f).
    // Begitu keyboard ditutup, proporsi DIJAMIN langsung kembali ke 1/4 layar (list: 3f).
    val isExpanded = isKeyboardOpen && notesFocused
    val listWeight by animateFloatAsState(
        targetValue = if (isExpanded) 1.5f else 3f,
        animationSpec = tween(durationMillis = 250),
        label = "listWeight"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                title = {
                    Column {
                        Text(contract.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${contract.checkedCount}/${contract.items.size} berkas",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showRename = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Ubah judul")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { pad ->
        Column(
            Modifier
                .padding(top = pad.calculateTopPadding())
                .fillMaxSize()
                .imePadding()
        ) {
            // ---------- Bagian atas (3/4): daftar berkas ----------
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(listWeight)
                    .fillMaxWidth()
                    .imeNestedScroll(),
            ) {

                itemsIndexed(contract.items, key = { _, it -> it.id }) { index, item ->
                    DocRow(
                        index = index + 1,
                        item = item,
                        onToggleChecked = { onToggleChecked(item.id) },
                        onCycleColor = { onCycleColor(item.id) },
                        onLongPress = { itemToDelete = item },
                        modifier = Modifier.animateItem(),
                    )
                }
                item(key = "add") {
                    OutlinedButton(
                        onClick = { showAddItem = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tambah berkas")
                    }
                }
            }

            // ---------- Bagian bawah (1/4): catatan, selalu terlihat ----------
            NotesPanel(
                contractId = contract.id,
                initial = contract.notes,
                onChange = onNotesChange,
                onFocusChange = { notesFocused = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }

    if (showRename) {
        TextInputDialog(
            title = "Ubah judul",
            label = "Judul pekerjaan / kontrak",
            confirmText = "Simpan",
            initial = contract.title,
            onDismiss = { showRename = false },
            onConfirm = { onRename(it); showRename = false },
        )
    }
    if (showAddItem) {
        TextInputDialog(
            title = "Tambah berkas",
            label = "Nama berkas",
            confirmText = "Tambah",
            onDismiss = { showAddItem = false },
            onConfirm = { onAddItem(it); showAddItem = false },
        )
    }
    itemToDelete?.let { item ->
        ConfirmDialog(
            title = "Hapus berkas?",
            message = "\"${item.name}\" akan dihapus dari daftar pekerjaan ini.",
            onDismiss = { itemToDelete = null },
            onConfirm = { onDeleteItem(item.id); itemToDelete = null },
        )
    }
}

@Composable
private fun DocRow(
    index: Int,
    item: DocItem,
    onToggleChecked: () -> Unit,
    onCycleColor: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalDocPalette.current
    val color = item.effectiveColor
    val bg by animateColorAsState(palette.background(color), label = "rowBg")
    val accent by animateColorAsState(palette.accent(color), label = "rowAccent")
    val shape = RoundedCornerShape(14.dp)
    // Selalu memakai callback terbaru di dalam gesture detector yang berumur panjang.
    val currentToggle by rememberUpdatedState(onToggleChecked)
    val currentLongPress by rememberUpdatedState(onLongPress)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(1.dp, accent.copy(alpha = if (color == DocColor.BASE) 1f else 0.6f), shape)
            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
    ) {
        // Tombol warna di kiri (Base -> Hijau -> Kuning -> Base)
        ColorButton(
            color = color,
            enabled = item.checked,
            onClick = onCycleColor,
        )
        Spacer(Modifier.width(8.dp))
        // Area judul & nomor: bisa diketuk untuk toggle ceklis, atau tekan lama untuk hapus berkas
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(item.id) {
                    detectTapGestures(
                        onTap = { currentToggle() },
                        onLongPress = { currentLongPress() }
                    )
                }
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                "$index.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(26.dp),
            )
            Text(
                item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.checked) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
        Checkbox(
            checked = item.checked,
            onCheckedChange = { onToggleChecked() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@Composable
private fun ColorButton(color: DocColor, enabled: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val palette = LocalDocPalette.current
    // Simpan referensi toast supaya ketukan beruntun tidak menumpuk antrian toast.
    val toastRef = remember { mutableStateOf<Toast?>(null) }
    val fill by animateColorAsState(
        if (color == DocColor.BASE) MaterialTheme.colorScheme.surface else palette.accent(color),
        label = "btnFill",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable {
                if (enabled) {
                    onClick()
                } else {
                    toastRef.value?.cancel()
                    toastRef.value = Toast.makeText(
                        context,
                        "Centang berkas dahulu untuk mengubah warna",
                        Toast.LENGTH_SHORT
                    ).also { it.show() }
                }
            }
            .alpha(if (enabled) 1f else 0.45f),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(26.dp)
                .background(fill, CircleShape)
                .border(2.dp, if (color == DocColor.BASE) palette.baseBorder else fill, CircleShape),
        ) {
            if (!enabled) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Terkunci",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

@Composable
private fun NotesPanel(
    contractId: String,
    initial: String,
    onChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // State lokal per kontrak, supaya posisi kursor tidak lompat saat autosave.
    var value by remember(contractId) { mutableStateOf(TextFieldValue(initial)) }
    val currentOnChange by rememberUpdatedState(onChange)

    // Autosave dengan jeda 400 ms setelah berhenti mengetik.
    // (ViewModel mengabaikan jika isi tidak berubah.)
    LaunchedEffect(contractId, value.text) {
        delay(400)
        currentOnChange(value.text)
    }

    // Simpan segera saat aplikasi diminimize (jaga-jaga app dimatikan sistem)...
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        currentOnChange(value.text)
    }

    // ...dan saat keluar dari halaman ini.
    DisposableEffect(contractId) {
        onDispose { currentOnChange(value.text) }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier,
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp)
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Catatan",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp),
            )

            Box(Modifier.weight(1f).fillMaxWidth()) {
                BasicTextField(
                    value = value,
                    onValueChange = { value = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxSize()
                        .onFocusChanged { onFocusChange(it.isFocused) },
                    decorationBox = { inner ->
                        if (value.text.isEmpty()) {
                            Text(
                                "Tulis catatan di sini…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                        inner()
                    },
                )
            }
        }
    }
}

