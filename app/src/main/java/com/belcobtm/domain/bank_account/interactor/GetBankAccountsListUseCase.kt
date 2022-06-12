package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.bank_account.BankAccountRepository
import com.belcobtm.domain.bank_account.item.BankAccountDataItem

class GetBankAccountsListUseCase(private val repository: BankAccountRepository) :
    UseCase<List<BankAccountDataItem>, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, List<BankAccountDataItem>> =
        repository.getBankAccountsList()
}