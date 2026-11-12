package com.inventory.scanner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.scanner.data.ScanDao
import com.inventory.scanner.data.ScanDatabase
import com.inventory.scanner.data.ScanEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: ScanDao = ScanDatabase.getInstance(application).scanDao()

    private val _scans = MutableStateFlow<List<ScanEntity>>(emptyList())
    val scans: StateFlow<List<ScanEntity>> = _scans.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect { _scans.value = it }
        }
    }

    fun addScan(value: String, format: String) {
        viewModelScope.launch {
            dao.insert(ScanEntity(value = value, format = format))
        }
    }

    fun clearAll() {
        viewModelScope.launch { dao.clearAll() }
    }
}
