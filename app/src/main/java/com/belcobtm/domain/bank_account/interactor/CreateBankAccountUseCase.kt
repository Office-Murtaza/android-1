package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.bank_account.BankAccountRepository
import com.belcobtm.domain.bank_account.item.BankAccountCreateDataItem
import com.belcobtm.domain.bank_account.item.BankAccountCreateResponseDataItem

class CreateBankAccountUseCase(
    private val repositoryImpl: BankAccountRepository,
) : UseCase<BankAccountCreateResponseDataItem, CreateBankAccountUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, BankAccountCreateResponseDataItem> {
        return repositoryImpl.createBankAccount(params.bankAccountCreateDataItem)
    }

    data class Params(val bankAccountCreateDataItem: BankAccountCreateDataItem)
}