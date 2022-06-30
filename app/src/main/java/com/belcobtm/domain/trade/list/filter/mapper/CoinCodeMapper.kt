package com.belcobtm.domain.trade.list.filter.mapper

import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.features.wallet.trade.list.filter.model.CoinCodeListItem

class CoinCodeMapper {

    fun map(accountEntity: AccountEntity, selectedCoinCode: String): CoinCodeListItem =
        with(accountEntity) {
            CoinCodeListItem(type.name, type.resIcon(), isEnabled, type.name == selectedCoinCode)
        }
}