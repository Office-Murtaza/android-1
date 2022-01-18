package com.belcobtm.presentation.features.wallet.trade.statistic

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.FragmentTradeUserInfoBinding
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.extensions.hide
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.wallet.trade.container.TradeContainerViewModel
import com.belcobtm.presentation.features.wallet.trade.list.model.TradeStatistics
import org.koin.androidx.viewmodel.ext.android.viewModel

class TradeUserStatisticFragment : BaseFragment<FragmentTradeUserInfoBinding>() {

    override val isToolbarEnabled: Boolean
        get() = false

    private val parentViewModel by lazy {
        requireParentFragment().viewModel<TradeContainerViewModel>().value
    }

    private val viewModel by viewModel<TradeUserStatisticViewModel>()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeUserInfoBinding =
        FragmentTradeUserInfoBinding.inflate(inflater, container, false)

    override fun updateActionBar() {

    }

    override fun initToolbar() {
        baseBinding.toolbarView.hide()
    }

    override fun FragmentTradeUserInfoBinding.initObservers() {
        viewModel.observeStatistic().observe(viewLifecycleOwner) {
            if (it?.isRight == true) {
                val statistic = (it as Either.Right<TradeStatistics>).b
                accountPublicId.text = statistic.publicId
                successRateValue.text = statistic.tradingRate.toString()
                totalTradesValue.text = statistic.totalTrades.toString()
            } else {
                parentViewModel.showError((it as Either.Left<Failure>).a)
            }
        }
    }
}