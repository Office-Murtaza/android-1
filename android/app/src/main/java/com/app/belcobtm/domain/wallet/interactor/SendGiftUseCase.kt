package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository

class SendGiftUseCase(private val repository: WalletRepository) : UseCase<Unit, SendGiftUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.sendGift(
        smsCode = params.smsCode,
        hash = params.hash,
        fromCoin = params.coinFrom,
        fromCoinAmount = params.coinFromAmount,
        giftId = params.giftId,
        phone = params.phone,
        message = params.message
    )

    data class Params(
        val smsCode: String,
        val hash: String,
        val coinFrom: String,
        val coinFromAmount: Double,
        val giftId: String,
        val phone: String,
        val message: String
    )
}