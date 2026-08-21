package com.descope.deviceauthdemo.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Renders [content] as a scannable QR code using ZXing, no server round-trip
 * or image download required.
 */
@Composable
fun QrCode(content: String, modifier: Modifier = Modifier, sizePx: Int = 512) {
    val bitmap = remember(content) { encodeQrBitmap(content, sizePx) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR code for $content",
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.size(180.dp), contentAlignment = Alignment.Center) {
            Text("QR unavailable", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun encodeQrBitmap(content: String, sizePx: Int): Bitmap? = try {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    bitmap
} catch (t: Throwable) {
    null
}
