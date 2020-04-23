package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository

class SendSmsToDeviceUseCase(private val repository: WalletRepository) : UseCase<Unit, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, Unit> = repository.sendSmsToDevice()
}