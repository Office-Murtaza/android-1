package com.app.belcobtm.presentation.features.wallet.trade.details

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.FragmentTradeDetailsBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment

class TradeDetailsFragment : BaseFragment<FragmentTradeDetailsBinding>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeDetailsBinding =
        FragmentTradeDetailsBinding.inflate(inflater, container, false)

    override fun FragmentTradeDetailsBinding.initListeners() {
        buySellButton.setOnClickListener {
            navigate(TradeDetailsFragmentDirections.toTradeDetailsBuySell())
        }
    }
}