package com.app.belcobtm.presentation.features.wallet.trade.mytrade

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.FragmentMyTradesBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment

class MyTradesFragment : BaseFragment<FragmentMyTradesBinding>() {

    // TODO add adapter and delegate

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyTradesBinding =
        FragmentMyTradesBinding.inflate(inflater, container, false)

    override fun FragmentMyTradesBinding.initViews() {
        TODO("Not yet implemented")
    }
}