package com.example.readinglistapp.scanner

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class IsbnScannerHelper(private val context: Context) {

    fun scanIsbn(
        onIsbnScanned: (String) -> Unit,
        onError: (String) -> Unit,
        onCanceled: () -> Unit = {}
    ) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13)
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (!rawValue.isNullOrEmpty()) {
                    onIsbnScanned(rawValue)
                } else {
                    onError("No valid barcode detected")
                }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Barcode scanning failed")
            }
            .addOnCanceledListener {
                onCanceled()
            }
    }
}
