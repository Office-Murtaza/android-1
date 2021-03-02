package com.app.belcobtm.presentation.features.wallet.trade.container.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.presentation.features.wallet.trade.list.TradeListFragment
import com.app.belcobtm.presentation.features.wallet.trade.statistic.TradeUserStatisticFragment

class TradeContainerViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    companion object {
        const val BUY_TRADES_TAB_POSITION = 0
        const val SELL_TRADES_TAB_POSITION = 1
        const val TRADE_INFO_TAB_POSITION = 2
    }

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment =
        when (position) {
            BUY_TRADES_TAB_POSITION -> TradeListFragment.newInstance(TradeType.BUY)
            SELL_TRADES_TAB_POSITION -> TradeListFragment.newInstance(TradeType.SELL)
            TRADE_INFO_TAB_POSITION -> TradeUserStatisticFragment()
            else -> throw RuntimeException("Illegal position of tab $position")
        }
}