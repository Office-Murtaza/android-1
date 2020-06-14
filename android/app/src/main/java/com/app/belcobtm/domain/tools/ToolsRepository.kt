package com.app.belcobtm.domain.tools

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

interface ToolsRepository {

    suspend fun sendSmsToDevice(): Either<Failure, Unit>

    suspend fun verifySmsCode(smsCode: String): Either<Failure, Unit>
}