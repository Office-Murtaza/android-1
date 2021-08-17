package com.belcobtm.presentation.features.wallet.trade.container.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.presentation.features.wallet.trade.info.TradeInfoContainerFragment
import com.belcobtm.presentation.features.wallet.trade.list.TradeListFragment

class TradeContainerViewPagerAdapter(
    private val isSellAvailable: Boolean,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    companion object {
        const val FIRST_TAB_POSITION = 0
        const val SECOND_TAB_POSITION = 1
        const val THIRD_TAB_POSITION = 2
    }

    override fun getItemCount(): Int = if (isSellAvailable) 3 else 2

    override fun createFragment(position: Int): Fragment =
        if (isSellAvailable) {
            when (position) {
                FIRST_TAB_POSITION -> TradeListFragment.newInstance(TradeType.SELL)
                SECOND_TAB_POSITION -> TradeListFragment.newInstance(TradeType.BUY)
                THIRD_TAB_POSITION -> TradeInfoContainerFragment()
                else -> throw RuntimeException("Illegal position of tab $position")
            }
        } else {
            when (position) {
                FIRST_TAB_POSITION -> TradeListFragment.newInstance(TradeType.SELL)
                SECOND_TAB_POSITION -> TradeInfoContainerFragment()
                else -> throw RuntimeException("Illegal position of tab $position")
            }
        }
}