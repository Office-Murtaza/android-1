package com.app.belcobtm.data.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

class FileHelper(private val context: Context) {

    fun createFilePart(filePath: String, fileMediaType: String, partName: String): MultipartBody.Part {
        val file = File(filePath)
        val body = RequestBody.create(MediaType.parse(fileMediaType), file)
        return MultipartBody.Part.createFormData(partName, file.name, body)
    }

    fun compressImageFile(uri: Uri): File {
        val bitmap: Bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        val scaledBitmap: Bitmap = scaleBitmap(bitmap)

        val dir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(dir, File(uri.path ?: "").name)
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            bitmap.recycle()
            scaledBitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }

    private fun scaleBitmap(bm: Bitmap): Bitmap {
        val bitmapWidth: Float = bm.width.toFloat()
        val bitmapHeight: Float = bm.height.toFloat()
        val width: Float
        val height: Float

        when {
            bitmapWidth > bitmapHeight -> {
                // landscape
                val ratio = bitmapWidth / IMAGE_MAX_WIDTH
                width = IMAGE_MAX_WIDTH
                height = bitmapHeight / ratio
            }
            bitmapHeight > bitmapWidth -> {
                // portrait
                val ratio = bitmapHeight / IMAGE_MAX_HEIGHT
                width = bitmapWidth / ratio
                height = IMAGE_MAX_HEIGHT
            }
            else -> {
                width = bitmapWidth
                height = bitmapHeight
            }
        }

        return Bitmap.createScaledBitmap(bm, width.toInt(), height.toInt(), true)
    }

    companion object {
        const val IMAGE_MAX_WIDTH = 1500F
        const val IMAGE_MAX_HEIGHT = 1500F
        const val IMAGE_QUALITY = 85
    }
}