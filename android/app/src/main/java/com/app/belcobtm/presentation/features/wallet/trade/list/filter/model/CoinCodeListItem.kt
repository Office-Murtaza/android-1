package com.app.belcobtm.presentation.features.wallet.trade.list.filter.model

import androidx.annotation.DrawableRes
import com.app.belcobtm.presentation.core.adapter.model.ListItem

data class CoinCodeListItem(
    val coinCode: String,
    @DrawableRes val coinIcon: Int,
    val enabled: Boolean,
    val selected: Boolean
) : ListItem {

    override val id: String
        get() = coinCode

    override val type: Int
        get() = COIN_CODE_LIST_ITEM_TYPE

    companion object {
        const val COIN_CODE_LIST_ITEM_TYPE = 5
    }

}