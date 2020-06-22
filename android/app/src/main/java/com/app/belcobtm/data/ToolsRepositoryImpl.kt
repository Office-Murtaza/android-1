package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.rest.tools.ToolsApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.tools.ToolsRepository

class ToolsRepositoryImpl(
    private val apiService: ToolsApiService,
    private val networkUtils: NetworkUtils
) : ToolsRepository {

    override suspend fun sendSmsToDevice(): Either<Failure, Unit> =
        if (networkUtils.isNetworkAvailable()) {
            apiService.sendToDeviceSmsCode()
        } else {
            Either.Left(Failure.NetworkConnection)
        }

    override suspend fun verifySmsCode(
        smsCode: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.verifySmsCode(smsCode)
    } else {
        Either.Left(Failure.NetworkConnection)
    }
}