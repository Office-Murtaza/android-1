package com.belcobtm.presentation.core

import android.graphics.Bitmap
import android.graphics.Color
import com.google.protobuf.ByteString
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.util.EnumMap

fun ByteArray.toHex(): String {
    return Numeric.toHexString(this)
}

fun String.toHexBytes(): ByteArray {
    return Numeric.hexStringToByteArray(this)
}

fun String.toHexByteArray(): ByteArray {
    return Numeric.hexStringToByteArray(this)
}

fun String.toByteString(): ByteString {
    return ByteString.copyFrom(this, Charsets.UTF_8)
}

fun String.toHexBytesInByteString(): ByteString {
    return ByteString.copyFrom(this.toHexBytes())
}

fun Long.toByteArray(): ByteArray {
    var newLong = this
    val result = ByteArray(8)
    for (i in 7 downTo 0) {
        result[i] = (newLong and 0xFF).toByte()
        newLong = newLong shr 8
    }
    return result
}

class QRUtils {
    companion object {

        fun getSpacelessQR(text: String, h: Int, w: Int): Bitmap? {
            try {
                /**
                 * Allow the zxing engine use the default argument for the margin variable
                 */
                val MARGIN_AUTOMATIC = -1

                /**
                 * Set no margin to be added to the QR code by the zxing engine
                 */
                val MARGIN_NONE = 0
                var hints: MutableMap<EncodeHintType?, Any?>? = null
                if (MARGIN_NONE != MARGIN_AUTOMATIC) {
                    hints = EnumMap(EncodeHintType::class.java)
                    // We want to generate with a custom margin size
                    hints[EncodeHintType.MARGIN] = MARGIN_NONE
                }
                val writer = MultiFormatWriter()
                val result =
                    writer.encode(text, BarcodeFormat.QR_CODE, h, w, hints)
                val width = result.width
                val height = result.height
                val pixels = IntArray(width * height)
                for (y in 0 until height) {
                    val offset = y * width
                    for (x in 0 until width) {
                        pixels[offset + x] =
                            if (result[x, y]) Color.BLACK else Color.TRANSPARENT
                    }
                }
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                return bitmap
            } catch (e: WriterException) {
                e.printStackTrace()
                return null
            }
        }
    }

}
