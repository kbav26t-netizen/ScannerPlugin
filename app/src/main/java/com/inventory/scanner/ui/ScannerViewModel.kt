package com.inventory.scanner.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.scanner.data.ScanDatabase
import com.inventory.scanner.data.ScanEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val db = ScanDatabase.getInstance(application)
    private val scanDao = db.scanDao()

    private val _scans = MutableStateFlow<List<ScanEntity>>(emptyList())
    val scans: StateFlow<List<ScanEntity>> = _scans.asStateFlow()

    init {
        viewModelScope.launch { scanDao.getAll().collect { list -> _scans.value = list } }
    }

    fun addScan(value: String, format: String) {
        viewModelScope.launch {
            var name = scanDao.getByBarcode(value).firstOrNull { it.userName.isNotBlank() }?.userName ?: ""
            if (name.isBlank()) {
                try {
                    val uri = Uri.parse("content://com.inventory.hub.provider/products")
                    val cursor = context.contentResolver.query(uri, null, "barcode=?", arrayOf(value), null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            name = it.getString(it.getColumnIndexOrThrow("name"))
                        }
                    }
                } catch (e: Exception) {}
            }
            scanDao.insert(ScanEntity(value = value, format = format, userName = name))
        }
    }

    fun updateQuantity(id: Long, quantity: Int) {
        viewModelScope.launch { scanDao.updateQuantity(id, quantity) }
    }

    fun updateUserNameByBarcode(barcode: String, userName: String) {
        viewModelScope.launch {
            val list = scanDao.getByBarcode(barcode)
            list.forEach { scan -> scanDao.update(scan.copy(userName = userName)) }
        }
    }

    fun deleteScan(id: Long) { viewModelScope.launch { scanDao.delete(id) } }
    fun clearAllScans() { viewModelScope.launch { scanDao.clearAll() } }
}
