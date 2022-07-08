package com.belcobtm.presentation.screens.wallet.trade.statistic

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.FragmentTradeUserInfoBinding
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.wallet.trade.container.TradeContainerViewModel
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeStatistics
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.formatter.Formatter
import com.belcobtm.presentation.tools.formatter.TraderRatingFormatter.Companion.TRADER_RATING_FORMATTER_QUALIFIER
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class TradeUserStatisticFragment : BaseFragment<FragmentTradeUserInfoBinding>() {

    override val isToolbarEnabled: Boolean
        get() = false

    private val parentViewModel by lazy {
        requireParentFragment().viewModel<TradeContainerViewModel>().value
    }

    private val viewModel by viewModel<TradeUserStatisticViewModel>()

    private val traderRatingFormatter: Formatter<Double> by inject(
        named(TRADER_RATING_FORMATTER_QUALIFIER)
    )

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
                successRateValue.text = traderRatingFormatter.format(statistic.tradingRate)
                totalTradesValue.text = statistic.totalTrades.toString()
            } else {
                parentViewModel.showError((it as Either.Left<Failure>).a)
            }
        }
    }

}
