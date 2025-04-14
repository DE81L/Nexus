package com.example.nexus.chatapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.nexus.chatapp.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions for convenience
 */

/**
 * Show a toast message
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Show an alert dialog
 */
fun Context.showAlert(
    title: String,
    message: String,
    positiveButton: String = getString(R.string.ok),
    negativeButton: String? = null,
    onPositiveClick: () -> Unit = {},
    onNegativeClick: () -> Unit = {}
) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setPositiveButton(positiveButton) { _, _ -> onPositiveClick() }
    negativeButton?.let {
        builder.setNegativeButton(it) { _, _ -> onNegativeClick() }
    }
    builder.show()
}

/**
 * Hide the keyboard
 */
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocus = currentFocus ?: View(this)
    imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
}

/**
 * Format a timestamp to a readable date/time string
 */
fun Long.formatDateTime(pattern: String = "dd MMM yyyy, HH:mm"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}

/**
 * Get a file URI using FileProvider
 */
fun File.getUriForFile(context: Context): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        this
    )
}

/**
 * Get file size in a human-readable format
 */
fun Long.formatFileSize(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> "${this / 1024} KB"
        else -> "${this / (1024 * 1024)} MB"
    }
}

/**
 * Open a file with the default app
 */
fun Context.openFile(file: File) {
    val uri = file.getUriForFile(this)
    val mime = contentResolver.getType(uri) ?: "*/*"
    
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, mime)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        showToast(getString(R.string.no_app_to_open_file))
    }
}

/**
 * Share a file
 */
fun Context.shareFile(file: File) {
    val uri = file.getUriForFile(this)
    val mime = contentResolver.getType(uri) ?: "*/*"
    
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = mime
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    
    startActivity(Intent.createChooser(intent, getString(R.string.share_file)))
}
