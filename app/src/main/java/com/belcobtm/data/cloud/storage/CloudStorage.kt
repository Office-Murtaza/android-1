package com.belcobtm.data.cloud.storage

import android.graphics.Bitmap

interface CloudStorage {

    suspend fun uploadBitmap(fileName: String, bitmap: Bitmap)

    suspend fun getLink(fileName: String): String
}