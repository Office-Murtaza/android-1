package com.belcobtm.domain.tools

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure

interface ToolsRepository {

    suspend fun sendSmsToDevice(phone: String): Either<Failure, String>

    @Deprecated("Old realization")
    suspend fun sendSmsToDeviceOld(): Either<Failure, Unit>

    suspend fun verifySmsCodeOld(smsCode: String): Either<Failure, Unit>
}