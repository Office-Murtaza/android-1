package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.domain.bank_account.BankAccountRepository
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObservePaymentsUseCase(
    private val bankAccountRepository: BankAccountRepository
) {
    fun invoke(params: Params): Flow<List<BankAccountPaymentListItem>> =
        bankAccountRepository.observePayments()
            .map { data ->
                data.payments.values.filter {
                    it.bankAccountId == params.bankAccountId
                }
            }.flowOn(Dispatchers.Default)

    data class Params(val bankAccountId: String)
}