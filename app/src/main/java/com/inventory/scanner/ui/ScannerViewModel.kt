package com.inventory.scanner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.scanner.data.CountEntity
import com.inventory.scanner.data.ProductEntity
import com.inventory.scanner.data.ScanDatabase
import com.inventory.scanner.data.ScanEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CountPoint(val x: Float, val y: Float)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ScanDatabase.getInstance(application)
    val productDao = db.productDao()
    val countDao = db.countDao()
    private val scanDao = db.scanDao()

    private val _scans = MutableStateFlow<List<ScanEntity>>(emptyList())
    val scans: StateFlow<List<ScanEntity>> = _scans.asStateFlow()

    private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
    val products: StateFlow<List<ProductEntity>> = _products.asStateFlow()

    private val _counts = MutableStateFlow<List<CountEntity>>(emptyList())
    val counts: StateFlow<List<CountEntity>> = _counts.asStateFlow()

    init {
        viewModelScope.launch {
            scanDao.getAll().collect { list -> _scans.value = list }
        }
        viewModelScope.launch {
            productDao.allProducts().collect { list -> _products.value = list }
        }
        viewModelScope.launch {
            countDao.getAll().collect { list -> _counts.value = list }
        }
    }

    fun addScan(value: String, format: String) {
        viewModelScope.launch {
            scanDao.insert(ScanEntity(value = value, format = format))
        }
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch { scanDao.delete(id) }
    }

    fun clearAllScans() {
        viewModelScope.launch { scanDao.clearAll() }
    }
    
    // Заглушки для будущего Этапа 3 (распознавание)
    fun startCounting(productId: Long, productName: String) {}
    fun addPoint(x: Float, y: Float) {}
    fun addProduct(name: String, category: String, photos: List<Pair<FloatArray, String>>) {}
}
