package com.app.belcobtm.presentation.features.wallet.trade.order.details

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.FragmentTradeOrderDetailsBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment

class TradeOrderDetailsFragment : BaseFragment<FragmentTradeOrderDetailsBinding>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeOrderDetailsBinding =
        FragmentTradeOrderDetailsBinding.inflate(inflater, container, false)
}