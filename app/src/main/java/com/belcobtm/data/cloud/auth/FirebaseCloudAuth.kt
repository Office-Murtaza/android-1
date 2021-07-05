package com.belcobtm.data.cloud.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseCloudAuth(private val auth: FirebaseAuth) : CloudAuth {

    override suspend fun currentUserExists(): Boolean = auth.currentUser != null

    override suspend fun authWithToken(token: String) = suspendCancellableCoroutine<Unit> { cont ->
        auth.signInWithCustomToken(token)
            .addOnSuccessListener {
                cont.resume(Unit)
            }.addOnFailureListener {
                cont.resumeWithException(it)
            }
    }
}