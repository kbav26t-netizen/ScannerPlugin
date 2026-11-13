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

    private val _countPoints = MutableStateFlow<List<CountPoint>>(emptyList())
    val countPoints: StateFlow<List<CountPoint>> = _countPoints.asStateFlow()

    private var currentCountingProductId: Long? = null
    private var currentCountingProductName: String = ""

    init {
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

    fun startCounting(productId: Long, productName: String) {
        currentCountingProductId = productId
        currentCountingProductName = productName
        _countPoints.value = emptyList()
    }

    fun addPoint(x: Float, y: Float) {
        _countPoints.value = _countPoints.value + CountPoint(x, y)
    }

    fun undoLastPoint() {
        val current = _countPoints.value
        if (current.isNotEmpty()) {
            _countPoints.value = current.dropLast(1)
        }
    }

    fun clearPoints() {
        _countPoints.value = emptyList()
    }

    fun saveCount() {
        val pid = currentCountingProductId ?: return
        val qty = _countPoints.value.size
        if (qty > 0) {
            viewModelScope.launch {
                countDao.insert(CountEntity(productId = pid, productName = currentCountingProductName, qty = qty))
            }
        }
        currentCountingProductId = null
        currentCountingProductName = ""
        _countPoints.value = emptyList()
    }

    fun cancelCounting() {
        currentCountingProductId = null
        currentCountingProductName = ""
        _countPoints.value = emptyList()
    }

    fun addProduct(name: String, category: String, photos: List<Pair<FloatArray, String>>) {
        viewModelScope.launch {
            productDao.addProduct(name, category, photos)
        }
    }

    fun clearAllScans() {
        viewModelScope.launch { scanDao.clearAll() }
    }
}
