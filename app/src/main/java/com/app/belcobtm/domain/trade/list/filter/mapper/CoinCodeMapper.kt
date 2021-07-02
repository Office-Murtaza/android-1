package com.app.belcobtm.domain.trade.list.filter.mapper

import com.app.belcobtm.data.disk.database.account.AccountEntity
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.CoinCodeListItem

class CoinCodeMapper {

    fun map(accountEntity: AccountEntity, selectedCoinCode: String): CoinCodeListItem =
        with(accountEntity) {
            CoinCodeListItem(type.name, type.resIcon(), isEnabled, type.name == selectedCoinCode)
        }
}