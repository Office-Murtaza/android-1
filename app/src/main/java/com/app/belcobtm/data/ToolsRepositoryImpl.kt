package com.app.belcobtm.data

import com.app.belcobtm.data.rest.tools.ToolsApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.tools.ToolsRepository

class ToolsRepositoryImpl(private val apiService: ToolsApiService) : ToolsRepository {

    override suspend fun sendSmsToDevice(
        phone: String
    ): Either<Failure, String> = apiService.verifyPhone(phone)

    override suspend fun sendSmsToDeviceOld(): Either<Failure, Unit> =
        apiService.sendToDeviceSmsCode()

    override suspend fun verifySmsCodeOld(
        smsCode: String
    ): Either<Failure, Unit> = apiService.verifySmsCode(smsCode)
}