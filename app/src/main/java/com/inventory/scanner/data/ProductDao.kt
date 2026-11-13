package com.inventory.scanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert
    suspend fun insertProduct(p: ProductEntity): Long

    @Insert
    suspend fun insertPhoto(photo: PhotoEntity)

    @Transaction
    suspend fun addProduct(name: String, category: String, photos: List<Pair<FloatArray, String>>): Long {
        val pid = insertProduct(ProductEntity(name = name, category = category))
        photos.forEach { (emb, thumb) ->
            insertPhoto(PhotoEntity(productId = pid, embedding = emb, thumbPath = thumb))
        }
        return pid
    }

    @Query("SELECT * FROM photos")
    suspend fun allPhotos(): List<PhotoEntity>

    @Query("SELECT * FROM products")
    fun allProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun productById(id: Long): ProductEntity?

    @Query("DELETE FROM products")
    suspend fun clearProducts()
}
