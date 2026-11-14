package com.inventory.scanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventory.scanner.ui.ScannerScreen
import com.inventory.scanner.ui.ScannerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel: ScannerViewModel = viewModel()
                    val context = LocalContext.current
                    ScannerScreen(
                        viewModel = viewModel,
                        onBack = { goBackToHub(context) }
                    )
                }
            }
        }
    }

    private fun goBackToHub(context: Context) {
        val hubIntent = context.packageManager.getLaunchIntentForPackage("com.inventory.hub")
        if (hubIntent != null) {
            hubIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(hubIntent)
        }
        (context as? Activity)?.finish()
    }
}
