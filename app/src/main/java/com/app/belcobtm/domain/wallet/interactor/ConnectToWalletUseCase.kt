package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase

class ConnectToWalletUseCase(
    private val walletConnectionHandler: WalletConnectionHandler
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        walletConnectionHandler.connect()
        return Either.Right(Unit)
    }
}