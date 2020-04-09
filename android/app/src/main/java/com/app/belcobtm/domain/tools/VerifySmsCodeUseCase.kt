package com.app.belcobtm.domain.tools

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository

class VerifySmsCodeUseCase(private val repository: WalletRepository) : UseCase<Unit, VerifySmsCodeUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.verifySmsCode(params.smsCode)

    data class Params(val smsCode: String)
}