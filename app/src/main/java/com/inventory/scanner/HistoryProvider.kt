package com.inventory.scanner

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.inventory.scanner.data.ScanDatabase

class HistoryProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "com.inventory.scanner.history"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/scans")

        private const val SCANS = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "scans", SCANS)
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        if (uriMatcher.match(uri) != SCANS) return null

        val context = context ?: return null
        val database = ScanDatabase.getInstance(context)
        val scans = database.scanDao().getAllSync()

        val cursor = MatrixCursor(arrayOf("_id", "barcode", "quantity", "format", "timestamp"))
        scans.forEachIndexed { index, scan ->
            cursor.newRow()
                .add(scan.id)
                .add(scan.value)
                .add(scan.quantity)
                .add(scan.format)
                .add(scan.timestamp)
        }
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.com.inventory.scanner.history"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
