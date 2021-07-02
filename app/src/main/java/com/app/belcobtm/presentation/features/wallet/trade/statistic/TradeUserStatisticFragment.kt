package com.app.belcobtm.presentation.features.wallet.trade.statistic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.app.belcobtm.databinding.FragmentTradeUserInfoBinding
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.container.TradeContainerViewModel
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeStatistics
import org.koin.android.viewmodel.ext.android.viewModel

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
                accountVerificationStatus.setCompoundDrawablesWithIntrinsicBounds(statistic.statusIcon, 0, 0, 0)
                accountVerificationStatus.setText(statistic.statusLabel)
                totalTradesValue.text = statistic.totalTrades.toString()
            } else {
                parentViewModel.showError((it as Either.Left<Failure>).a)
            }
        }
    }
}