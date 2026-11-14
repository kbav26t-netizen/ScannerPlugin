package com.inventory.scanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scans ADD COLUMN userName TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [ScanEntity::class, ProductEntity::class, PhotoEntity::class, CountEntity::class],
    version = 4,
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
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
    }
}
