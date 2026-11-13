package com.inventory.scanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "counts")
data class CountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val qty: Int,
    val timestamp: Long = System.currentTimeMillis()
)
