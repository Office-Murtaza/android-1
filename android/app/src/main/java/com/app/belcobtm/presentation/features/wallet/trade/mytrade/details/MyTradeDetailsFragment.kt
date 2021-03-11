package com.app.belcobtm.presentation.features.wallet.trade.mytrade.details

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.FragmentTradeDetailsBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment

class MyTradeDetailsFragment : BaseFragment<FragmentTradeDetailsBinding>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeDetailsBinding =
        FragmentTradeDetailsBinding.inflate(inflater, container, false)


}