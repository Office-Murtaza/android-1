package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.bank_account.BankAccountRepository

class GetLinkTokenUseCase(private val repository: BankAccountRepository) :
    UseCase<String, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, String> =
        repository.getLinkToken()
}