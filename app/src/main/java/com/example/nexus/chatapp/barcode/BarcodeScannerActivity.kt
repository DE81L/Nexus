package com.example.nexus.chatapp.barcode

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.databinding.ActivityBarcodeScannerBinding
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.ThemeManager
import com.example.nexus.chatapp.utils.showAlert
import com.example.nexus.chatapp.utils.showToast
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatReader
import com.google.zxing.Result
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarcodeScannerBinding
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager
    
    private lateinit var cameraExecutor: ExecutorService
    private var isScannerActive = false
    private var lastScannedResult: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize managers
        themeManager = ThemeManager(this)
        languageManager = LanguageManager(this)
        
        // Apply theme and language
        themeManager.applyThemeForActivity(this)
        languageManager.applyLanguageForActivity(this)
        
        // Use view binding
        binding = ActivityBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.barcode_scanner)
        
        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Request camera permission if not granted
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        
        // Setup button to resume scanning
        binding.buttonResume.setOnClickListener {
            lastScannedResult = null
            binding.resultContainer.visibility = View.GONE
            binding.previewView.visibility = View.VISIBLE
            isScannerActive = true
        }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            
            // Image analysis for barcode scanning
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcodeValue ->
                        if (isScannerActive && lastScannedResult == null) {
                            lastScannedResult = barcodeValue
                            isScannerActive = false
                            
                            runOnUiThread {
                                showBarcodeResult(barcodeValue)
                            }
                        }
                    })
                }
            
            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                // Unbind previous use cases before binding
                cameraProvider.unbindAll()
                
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
                
                isScannerActive = true
                
            } catch (exc: Exception) {
                showToast(getString(R.string.camera_error))
            }
            
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun showBarcodeResult(result: String) {
        binding.previewView.visibility = View.GONE
        binding.resultContainer.visibility = View.VISIBLE
        binding.textBarcodeResult.text = result
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                showAlert(
                    title = getString(R.string.permission_required),
                    message = getString(R.string.camera_permission_required),
                    positiveButton = getString(R.string.ok),
                    onPositiveClick = { finish() }
                )
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    
    // Barcode analyzer class
    private class BarcodeAnalyzer(private val onBarcodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
        private val multiFormatReader = MultiFormatReader()
        
        init {
            // Configure the reader for common barcode formats
            val formats = mapOf(
                BarcodeFormat.QR_CODE to true,
                BarcodeFormat.CODE_39 to true,
                BarcodeFormat.CODE_93 to true,
                BarcodeFormat.CODE_128 to true,
                BarcodeFormat.EAN_13 to true,
                BarcodeFormat.EAN_8 to true,
                BarcodeFormat.UPC_A to true,
                BarcodeFormat.UPC_E to true
            )
            
            val hints = mapOf(
                com.google.zxing.DecodeHintType.POSSIBLE_FORMATS to formats.filterValues { it }.keys.toList()
            )
            
            multiFormatReader.setHints(hints)
        }
        
        override fun analyze(image: ImageAnalysis.ImageProxy) {
            image.use { imageProxy ->
                val buffer = imageProxy.planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                
                val width = imageProxy.width
                val height = imageProxy.height
                
                // Convert YUV to RGB
                val source = com.google.zxing.PlanarYUVLuminanceSource(
                    data,
                    width,
                    height,
                    0,
                    0,
                    width,
                    height,
                    false
                )
                
                val bitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
                
                try {
                    val result: Result = multiFormatReader.decode(bitmap)
                    onBarcodeDetected(result.text)
                } catch (e: Exception) {
                    // No barcode found in this frame
                }
            }
        }
    }
}
