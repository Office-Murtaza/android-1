package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.SettingsRepository

class UnlinkUseCase(
    private val walletConnectionHandler: WalletConnectionHandler,
    private val repository: SettingsRepository
) : UseCase<Boolean, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Boolean> {
        val unlink = repository.unlink()
        if (unlink.isRight && (unlink as Either.Right<Boolean>).b) {
            walletConnectionHandler.disconnect()
        }
        return unlink
    }
}