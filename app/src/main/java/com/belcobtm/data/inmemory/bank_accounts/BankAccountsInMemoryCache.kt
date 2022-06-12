package com.belcobtm.data.inmemory.bank_accounts

import com.belcobtm.data.model.bank_account.BankAccountsData
import com.belcobtm.data.rest.bank_account.response.BankAccountResponse
import com.belcobtm.data.rest.bank_account.response.mapToDataItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class BankAccountsInMemoryCache {

    private val cache = MutableStateFlow(BankAccountsData(emptyMap()))

    val observableData: Flow<BankAccountsData>
        get() = cache

    fun init(response: List<BankAccountResponse>) {
        val transactions = response.associateByTo(
            HashMap(), { (it.id).orEmpty() }) { it.mapToDataItem() }
        cache.value = BankAccountsData(
            cache.value.bankAccounts.toMutableMap().apply {
                putAll(transactions)
            }
        )
    }

    fun update(response: BankAccountResponse) {
        val bankAccounts = HashMap(cache.value.bankAccounts)
        val id = response.id
        id?.let {
            bankAccounts[id] = response.mapToDataItem()
            cache.value = cache.value.copy(bankAccounts = bankAccounts)
        }
    }
}