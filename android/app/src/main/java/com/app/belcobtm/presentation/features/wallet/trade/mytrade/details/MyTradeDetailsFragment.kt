package com.app.belcobtm.presentation.features.wallet.trade.mytrade.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.databinding.FragmentMyTradeDetailsBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment

class MyTradeDetailsFragment : BaseFragment<FragmentMyTradeDetailsBinding>() {

    val args by navArgs<MyTradeDetailsFragmentArgs>()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyTradeDetailsBinding =
        FragmentMyTradeDetailsBinding.inflate(inflater, container, false)

    override fun FragmentMyTradeDetailsBinding.initListeners() {
        editButton.setOnClickListener {
            navigate(MyTradeDetailsFragmentDirections.toEditMyTradeDetails(args.tradeId))
        }
    }
}