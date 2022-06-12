package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.domain.bank_account.BankAccountRepository
import com.belcobtm.domain.bank_account.item.BankAccountDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveBankAccountsListUseCase(
    private val bankAccountRepository: BankAccountRepository
) {
    fun invoke(): Flow<List<BankAccountDataItem>> =
        bankAccountRepository.observeBankAccounts()
            .map { data ->
                data.bankAccounts.values.toList()
            }.flowOn(Dispatchers.Default)

}