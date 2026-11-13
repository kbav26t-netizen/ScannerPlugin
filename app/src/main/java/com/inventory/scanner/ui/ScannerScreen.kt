package com.inventory.scanner.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.inventory.scanner.BarcodeAnalyzer
import com.inventory.scanner.data.ScanEntity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(viewModel: ScannerViewModel, onBack: () -> Unit = {}) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val scans by viewModel.scans.collectAsState()
    val context = LocalContext.current

    var showHistory by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    var lastScanTime by remember { mutableStateOf(0L) }

    if (showHistory) {
        HistoryView(
            scans = scans,
            onDelete = { viewModel.deleteScan(it) },
            onClearAll = { viewModel.clearAllScans() },
            onBack = { showHistory = false },
            onQuantityChange = { id, qty -> viewModel.updateQuantity(id, qty) }
        )
    } else if (cameraPermissionState.status.isGranted) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Сканер") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "В меню")
                    }
                },
                actions = {
                    IconButton(onClick = { torchOn = !torchOn }) {
                        Icon(
                            if (torchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Фонарик",
                            tint = if (torchOn) Color.Yellow else Color.White
                        )
                    }
                    IconButton(onClick = { exportScans(context, scans) }) {
                        Icon(Icons.Default.Share, contentDescription = "Экспорт")
                    }
                    IconButton(onClick = { showHistory = true }) {
                        Icon(Icons.Default.History, contentDescription = "История")
                    }
                }
            )
            CameraPreview(
                onBarcodeDetected = { code ->
                    val now = System.currentTimeMillis()
                    if (code != lastScannedCode || (now - lastScanTime) > 2000) {
                        lastScannedCode = code
                        lastScanTime = now
                        viewModel.addScan(code, "BARCODE")
                        vibrate(context)
                    }
                },
                torchOn = torchOn,
                modifier = Modifier.weight(1f)
            )
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Последний штрих-код:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val lastScan = scans.firstOrNull()
                    Text(
                        lastScan?.value ?: "Наведите камеру на штрих-код",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (lastScan != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Формат: ${lastScan.format} • Кол-во: ${lastScan.quantity}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text("Всего отсканировано: ${scans.size}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Разрешить доступ к камере")
            }
        }
    }
}

@Composable
fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    torchOn: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    val cameraState = remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(lifecycleOwner) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(analyzerExecutor, BarcodeAnalyzer(onBarcodeDetected))
            }
        try {
            cameraProvider.unbindAll()
            cameraState.value = cameraProvider.bindToLifecycle(
                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            cameraProvider.unbindAll()
            analyzerExecutor.shutdown()
        }
    }

    LaunchedEffect(torchOn, cameraState.value) {
        cameraState.value?.cameraControl?.enableTorch(torchOn)
    }

    AndroidView(factory = { previewView }, modifier = modifier.fillMaxWidth())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryView(
    scans: List<ScanEntity>,
    onDelete: (Long) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit,
    onQuantityChange: (Long, Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История (${scans.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (scans.isNotEmpty()) {
                        IconButton(onClick = onClearAll) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить всё")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (scans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("История пуста", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scans, key = { it.id }) { record ->
                    ScanRecordCard(
                        record = record,
                        onDelete = { onDelete(record.id) },
                        onQuantityChange = { newQty -> onQuantityChange(record.id, newQty) }
                    )
                }
            }
        }
    }
}

@Composable
fun ScanRecordCard(record: ScanEntity, onDelete: () -> Unit, onQuantityChange: (Int) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${record.format} • ${dateFormat.format(java.util.Date(record.timestamp))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { if (record.quantity > 1) onQuantityChange(record.quantity - 1) }, modifier = Modifier.size(36.dp)) {
                Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text("${record.quantity}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = { onQuantityChange(record.quantity + 1) }, modifier = Modifier.size(36.dp)) {
                Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Clear, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun vibrate(context: android.content.Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val mgr = context.getSystemService(VibratorManager::class.java)
        mgr?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }
    if (vibrator?.hasVibrator() == true) {
        vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

private fun exportScans(context: android.content.Context, scans: List<ScanEntity>) {
    if (scans.isEmpty()) {
        Toast.makeText(context, "Нечего экспортировать", Toast.LENGTH_SHORT).show()
        return
    }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    val text = scans.joinToString("\n") { "${it.value}\t${it.quantity}\t${it.format}\t${dateFormat.format(java.util.Date(it.timestamp))}" }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "Результаты сканирования (${scans.size})")
    }
    context.startActivity(Intent.createChooser(intent, "Экспорт истории"))
}
