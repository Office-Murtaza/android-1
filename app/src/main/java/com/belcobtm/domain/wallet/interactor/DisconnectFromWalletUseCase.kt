package com.belcobtm.domain.wallet.interactor

import com.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class DisconnectFromWalletUseCase(
    private val walletConnectionHandler: WalletConnectionHandler
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        walletConnectionHandler.disconnect()
        return Either.Right(Unit)
    }
}