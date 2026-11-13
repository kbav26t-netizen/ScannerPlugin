package com.inventory.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventory.scanner.ui.CountingScreen
import com.inventory.scanner.ui.ModeSelectionScreen
import com.inventory.scanner.ui.ScannerScreen
import com.inventory.scanner.ui.ScannerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel: ScannerViewModel = viewModel()
                    var currentScreen by remember { mutableStateOf("home") }

                    when (currentScreen) {
                        "home" -> ModeSelectionScreen(
                            onBarcodeModeClick = { currentScreen = "barcode" },
                            onCountingModeClick = { currentScreen = "count" }
                        )
                        "barcode" -> ScannerScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "home" }
                        )
                        "count" -> CountingScreen(
                            onBack = { currentScreen = "home" }
                        )
                    }
                }
            }
        }
    }
}
