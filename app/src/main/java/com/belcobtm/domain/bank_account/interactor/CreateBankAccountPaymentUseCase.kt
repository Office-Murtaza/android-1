package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.bank_account.BankAccountRepository
import com.belcobtm.domain.bank_account.item.BankAccountCreatePaymentDataItem
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem

class CreateBankAccountPaymentUseCase(
    private val repositoryImpl: BankAccountRepository,
) : UseCase<BankAccountPaymentListItem, CreateBankAccountPaymentUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, BankAccountPaymentListItem> {
        return repositoryImpl.createBankAccountPayment(params.bankAccountCreatePaymentDataItem)
    }

    data class Params(val bankAccountCreatePaymentDataItem: BankAccountCreatePaymentDataItem)
}