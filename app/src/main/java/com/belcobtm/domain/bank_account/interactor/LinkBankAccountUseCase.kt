package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.bank_account.BankAccountRepository
import com.belcobtm.domain.bank_account.item.BankAccountDataItem
import com.belcobtm.domain.bank_account.item.BankAccountLinkDataItem

class LinkBankAccountUseCase(
    private val repositoryImpl: BankAccountRepository,
) : UseCase<List<BankAccountDataItem>, LinkBankAccountUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, List<BankAccountDataItem>> {
        return repositoryImpl.linkBankAccount(params.bankAccountLinkDataItem)
    }

    data class Params(val bankAccountLinkDataItem: BankAccountLinkDataItem)
}