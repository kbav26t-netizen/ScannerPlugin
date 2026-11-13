package com.inventory.scanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromFloatArray(arr: FloatArray): ByteArray {
        val buf = java.nio.ByteBuffer.allocate(arr.size * 4)
        arr.forEach { buf.putFloat(it) }
        return buf.array()
    }

    @TypeConverter
    fun toFloatArray(bytes: ByteArray): FloatArray {
        val buf = java.nio.ByteBuffer.wrap(bytes)
        return FloatArray(bytes.size / 4) { buf.getFloat() }
    }
}

@Database(
    entities = [ScanEntity::class, ProductEntity::class, PhotoEntity::class, CountEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun productDao(): ProductDao
    abstract fun countDao(): CountDao

    companion object {
        @Volatile private var INSTANCE: ScanDatabase? = null
        fun getInstance(context: Context): ScanDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext, ScanDatabase::class.java, "scanner.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
