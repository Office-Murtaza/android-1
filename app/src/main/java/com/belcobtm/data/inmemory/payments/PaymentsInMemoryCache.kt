package com.belcobtm.data.inmemory.payments

import com.belcobtm.data.model.payments.PaymentsData
import com.belcobtm.data.rest.bank_account.response.BankAccountPayment
import com.belcobtm.data.rest.bank_account.response.mapToDataItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PaymentsInMemoryCache {
    private val cache = MutableStateFlow(PaymentsData(emptyMap()))

    val observableData: Flow<PaymentsData>
        get() = cache

    fun init(response: List<BankAccountPayment>) {
        val payments = response.associateByTo(
            HashMap(), { it.id }) { it.mapToDataItem() }
        cache.value = PaymentsData(
            cache.value.payments.toMutableMap().apply {
                putAll(payments)
            }
        )
    }
    fun update(response: BankAccountPayment?) {
        val payments = HashMap(cache.value.payments)
        val id = response?.id
        id?.let {
            payments[id] = response.mapToDataItem()
            cache.value = cache.value.copy(payments = payments)
        }
    }
}