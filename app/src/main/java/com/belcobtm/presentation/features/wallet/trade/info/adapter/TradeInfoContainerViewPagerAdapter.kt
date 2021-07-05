package com.belcobtm.presentation.features.wallet.trade.info.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.belcobtm.presentation.features.wallet.trade.mytrade.list.MyTradesFragment
import com.belcobtm.presentation.features.wallet.trade.order.TradeOrdersFragment
import com.belcobtm.presentation.features.wallet.trade.statistic.TradeUserStatisticFragment

class TradeInfoContainerViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    companion object {
        const val MY_TRADES_TAB_POSITION = 0
        const val ORDERS_TAB_POSITION = 1
        const val TRADE_INFO_TAB_POSITION = 2
    }

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment =
        when (position) {
            MY_TRADES_TAB_POSITION -> MyTradesFragment()
            ORDERS_TAB_POSITION -> TradeOrdersFragment()
            TRADE_INFO_TAB_POSITION -> TradeUserStatisticFragment()
            else -> throw RuntimeException("Illegal position of tab $position")
        }
}