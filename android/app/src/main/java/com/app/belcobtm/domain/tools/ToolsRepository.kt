package com.app.belcobtm.domain.tools

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

interface ToolsRepository {

    suspend fun sendSmsToDevice(phone: String): Either<Failure, String>

    suspend fun sendSmsToDeviceOld(): Either<Failure, Unit>

    suspend fun verifySmsCodeOld(smsCode: String): Either<Failure, Unit>
}