package com.biprangshu.guardiansathi.Guardian.core

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

fun scanQrFromUri(
    context: Context,
    uri: Uri,
    onScanned: (String) -> Unit,
    onFailure: (String) -> Unit = {}
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val result = barcodes.firstOrNull { it.rawValue != null }?.rawValue
                if (result != null) {
                    onScanned(result)
                } else {
                    onFailure("No QR code found in image")
                }
            }
            .addOnFailureListener {
                onFailure("Failed to scan image: ${it.message}")
            }
    } catch (e: Exception) {
        onFailure("Could not read image: ${e.message}")
    }
}