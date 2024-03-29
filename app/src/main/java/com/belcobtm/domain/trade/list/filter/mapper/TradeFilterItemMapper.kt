package com.belcobtm.domain.trade.list.filter.mapper

import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.domain.trade.create.mapper.PaymentIdToAvailablePaymentOptionMapper
import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.domain.trade.model.filter.SortOption
import com.belcobtm.domain.trade.model.filter.TradeFilter
import com.belcobtm.presentation.screens.wallet.trade.list.filter.model.TradeFilterItem

class TradeFilterItemMapper(
    private val coinMapper: CoinCodeMapper,
    private val paymentMapper: PaymentIdToAvailablePaymentOptionMapper
) {

    fun map(payments: List<PaymentMethodType>, entities: List<AccountEntity>, filter: TradeFilter?): TradeFilterItem {
        val availablePayments = payments.map {
            paymentMapper.map(it).copy(selected = filter?.paymentOptions?.contains(it) ?: false)
        }
        val coins = entities.map { coinMapper.map(it, filter?.coinCode.orEmpty()) }
        return TradeFilterItem(
            coins, availablePayments,
            filter?.filterByDistanceEnabled ?: false, filter?.minDistance ?: 0,
            filter?.maxDistance ?: 0, filter?.sortOption ?: SortOption.PRICE
        )
    }

}
