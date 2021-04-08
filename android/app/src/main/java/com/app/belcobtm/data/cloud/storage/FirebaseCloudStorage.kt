package com.app.belcobtm.data.cloud.storage

import android.graphics.Bitmap
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseCloudStorage(private val storageReference: StorageReference) : CloudStorage {

    companion object {
        const val CHAT_STORAGE = "chat_storage"
    }

    override suspend fun uploadBitmap(fileName: String, bitmap: Bitmap): Unit =
        suspendCancellableCoroutine { continuation ->
            val imageReference = storageReference.child(fileName)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            imageReference.putBytes(data)
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }.addOnSuccessListener {
                    continuation.resume(Unit)
                }
        }

    override suspend fun getLink(fileName: String): String =
        suspendCancellableCoroutine { continuation ->
            storageReference.child(fileName).downloadUrl
                .addOnSuccessListener {
                    continuation.resume(it.toString())
                }.addOnCanceledListener {
                    continuation.cancel()
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
}