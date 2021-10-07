package com.belcobtm.data

import com.belcobtm.data.rest.tools.ToolsApiService
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.tools.ToolsRepository

class ToolsRepositoryImpl(private val apiService: ToolsApiService) : ToolsRepository {

    override suspend fun sendSmsToDevice(phone: String): Either<Failure, Boolean> =
        apiService.sendSms(phone)

    override suspend fun verifySmsCode(phone: String, code: String): Either<Failure, Boolean> =
        apiService.verifySmsCode(phone, code)

    override suspend fun sendSmsToDeviceOld(): Either<Failure, Unit> =
        apiService.sendToDeviceSmsCode()

    override suspend fun verifySmsCodeOld(
        smsCode: String
    ): Either<Failure, Unit> = apiService.verifySmsCodeOld(smsCode)
}