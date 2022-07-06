package com.belcobtm.presentation.screens.atm

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemAtmInfoPopupOpenHoursBinding
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate

class AtmInfoPopupOpenHoursDelegate :
    AdapterDelegate<OpenHoursItem, AtmInfoPopupOpenHoursViewHolder>() {

    override val viewType: Int
        get() = OpenHoursItem.OPEN_HOURS_ITEM_LIST_TYPE

    override fun createHolder(
        parent: ViewGroup,
        inflater: LayoutInflater
    ): AtmInfoPopupOpenHoursViewHolder =
        AtmInfoPopupOpenHoursViewHolder(
            ItemAtmInfoPopupOpenHoursBinding.inflate(inflater, parent, false)
        )
}