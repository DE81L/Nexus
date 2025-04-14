package com.example.nexus.chatapp.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Utility class for file operations
 */
object FileUtils {
    
    /**
     * Create a temporary image file
     */
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp"
        val storageDir = File(context.filesDir, "images")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    /**
     * Create a directory for files if it doesn't exist
     */
    fun getFilesDirectory(context: Context): File {
        val filesDir = File(context.filesDir, "files")
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
        return filesDir
    }
    
    /**
     * Save a bitmap to a file
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File, quality: Int = 100): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Copy a file from a URI to app's internal storage
     */
    fun copyFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileNameFromUri(context, uri) ?: UUID.randomUUID().toString()
            
            val file = File(getFilesDirectory(context), fileName)
            
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024) // 4K buffer
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
            
            inputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get file name from Uri
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = it.getString(displayNameIndex)
                    }
                }
            }
        }
        
        if (fileName == null) {
            fileName = uri.path
            val cut = fileName?.lastIndexOf('/')
            if (cut != -1) {
                fileName = fileName?.substring(cut!! + 1)
            }
        }
        
        return fileName
    }
    
    /**
     * Get file size from Uri
     */
    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        var fileSize: Long = 0
        
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        fileSize = it.getLong(sizeIndex)
                    }
                }
            }
        }
        
        // If couldn't get size from column, try to get from stream
        if (fileSize == 0L) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    fileSize = inputStream.available().toLong()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return fileSize
    }
    
    /**
     * Load bitmap from file with sampling to prevent OOM errors
     */
    fun loadBitmapFromFile(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, options)
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }
    
    /**
     * Calculate sample size for loading a downsampled version of an image
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Create a file from an input stream
     */
    fun createFileFromInputStream(inputStream: InputStream, outputFile: File): Boolean {
        return try {
            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(4 * 1024) // 4K buffer
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get MIME type from file path
     */
    fun getMimeType(filePath: String): String {
        val extension = filePath.substringAfterLast('.', "")
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}
