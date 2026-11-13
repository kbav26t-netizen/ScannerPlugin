package com.inventory.scanner.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    foreignKeys = [ForeignKey(
        entity = ProductEntity::class,
        parentColumns = ["id"],
        childColumns = ["productId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("productId")]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val embedding: FloatArray,
    val thumbPath: String
)
