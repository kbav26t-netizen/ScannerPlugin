package com.inventory.scanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val value: String,
    val format: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quantity: Int = 1,
    val userName: String = ""
)
