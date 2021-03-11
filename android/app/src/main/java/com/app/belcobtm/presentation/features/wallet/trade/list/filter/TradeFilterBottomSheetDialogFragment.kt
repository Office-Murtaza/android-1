package com.app.belcobtm.presentation.features.wallet.trade.list.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.app.belcobtm.databinding.FragmentTradeFilterBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.features.wallet.trade.create.delegate.TradePaymentOptionDelegate
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.delegate.ItemTradeFilterCoinCodeDelegate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.viewmodel.ext.android.viewModel

class TradeFilterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val coinsAdapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply { registerDelegate(ItemTradeFilterCoinCodeDelegate(viewModel::selectCoin)) }
    }

    private val paymentsAdapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply { registerDelegate(TradePaymentOptionDelegate()) }
    }

    private val viewModel by viewModel<TradeFilterViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentTradeFilterBinding.inflate(inflater, container, false)
        binding.coins.adapter = coinsAdapter
        binding.paymentOptions.adapter = paymentsAdapter
        viewModel.fetchInitialData()
        viewModel.coins.observe(viewLifecycleOwner, coinsAdapter::update)
        viewModel.paymentOptions.observe(viewLifecycleOwner, paymentsAdapter::update)
        return binding.root
    }
}