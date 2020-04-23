package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository

class GetGiftAddressUseCase(private val repository: WalletRepository) :
    UseCase<String, GetGiftAddressUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.getGiftAddress(params.coinFrom, params.phone)

    data class Params(val coinFrom: String, val phone: String)
}