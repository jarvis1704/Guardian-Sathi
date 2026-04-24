package com.biprangshu.guardiansathi.Global.Elder.core

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun generateQrCodeAsBitmap(content: String, size: Int = 512): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}



fun shareQrCodeToWhatsApp(context: Context, bitmap: Bitmap, message: String) {
    try {
        // 1. Save bitmap to cache
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val imageFile = File(cachePath, "qr_code.png")
        FileOutputStream(imageFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        // 2. Get URI via FileProvider
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        // 3. Build WhatsApp intent
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")          // targets WhatsApp directly
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 4. Fallback if WhatsApp isn't installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Open generic share sheet instead
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallback, "Share QR Code via"))
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun resolveContactName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(
        uri, arrayOf(ContactsContract.Contacts.DISPLAY_NAME), null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst())
            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
        else null
    }
}

fun resolveContactPhone(context: Context, uri: Uri): String? {
    val contactId = context.contentResolver.query(
        uri, arrayOf(ContactsContract.Contacts._ID), null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst())
            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
        else null
    } ?: return null

    return context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
        arrayOf(contactId),
        null
    )?.use { cursor ->
        if (cursor.moveToFirst())
            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
        else null
    }
}