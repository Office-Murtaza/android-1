package com.app.belcobtm.data.rest.authorization

import com.app.belcobtm.data.rest.authorization.request.RecoverWalletRequest
import com.app.belcobtm.data.rest.authorization.request.VerifySmsCodeRequest
import com.app.belcobtm.data.rest.authorization.response.RecoverWalletResponse
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("recover")
    fun recoverWalletAsync(
        @Body request: RecoverWalletRequest
    ): Deferred<Response<RecoverWalletResponse>>

    @POST("user/{userId}/code/verify")
    fun verifySmsCodeAsync(
        @Path("userId") userId: Int,
        @Body request: VerifySmsCodeRequest
    ): Deferred<Response<ResponseBody>>
}