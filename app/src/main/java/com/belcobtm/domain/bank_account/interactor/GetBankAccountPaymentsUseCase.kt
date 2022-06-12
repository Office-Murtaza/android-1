package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.bank_account.BankAccountRepository
import com.belcobtm.domain.bank_account.item.BankAccountPaymentDataItem

class GetBankAccountPaymentsUseCase(private val repository: BankAccountRepository) :
    UseCase<BankAccountPaymentDataItem, GetBankAccountPaymentsUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, BankAccountPaymentDataItem> =
        repository.getBankAccountPayments(params.accountId)

    data class Params(val accountId: String)
}
