package com.app.belcobtm.presentation.features.wallet.trade.info

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.FragmentTradeInfoContainerBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment

class TradeInfoContainerFragment : BaseFragment<FragmentTradeInfoContainerBinding>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeInfoContainerBinding =
        FragmentTradeInfoContainerBinding.inflate(inflater, container, false)

    override fun FragmentTradeInfoContainerBinding.initViews() {

    }
}