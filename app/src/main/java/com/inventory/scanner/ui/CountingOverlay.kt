package com.inventory.scanner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CountingOverlay(
    currentProductName: String,
    points: List<CountPoint>,
    onAddPoint: (Float, Float) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onAddPoint(offset.x, offset.y)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.4f),
                    radius = 40f,
                    center = Offset(point.x, point.y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 25f,
                    center = Offset(point.x, point.y)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    (index + 1).toString(),
                    point.x,
                    point.y + 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 32f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Подсчёт:", color = Color.White, fontSize = 14.sp)
                Text(currentProductName, color = Color.Cyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "${points.size} шт.",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Отмена")
            }
            
            Button(
                onClick = onUndo,
                enabled = points.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Undo, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Назад")
            }

            Button(
                onClick = onSave,
                enabled = points.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Сохранить")
            }
        }
    }
}
