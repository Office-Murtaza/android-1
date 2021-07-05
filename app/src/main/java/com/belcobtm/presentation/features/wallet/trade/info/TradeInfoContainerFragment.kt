package com.belcobtm.presentation.features.wallet.trade.info

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeInfoContainerBinding
import com.belcobtm.presentation.core.extensions.hide
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.wallet.trade.info.adapter.TradeInfoContainerViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class TradeInfoContainerFragment : BaseFragment<FragmentTradeInfoContainerBinding>() {

    override val isToolbarEnabled: Boolean
        get() = false

    override val isHomeButtonEnabled: Boolean
        get() = false

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeInfoContainerBinding =
        FragmentTradeInfoContainerBinding.inflate(inflater, container, false)

    override fun updateActionBar() {

    }

    override fun initToolbar() {
        baseBinding.toolbarView.hide()
    }

    fun preselectScreen(position: Int) {
        binding.viewPager.post {
            binding.viewPager.currentItem = position
        }
    }

    override fun FragmentTradeInfoContainerBinding.initViews() {
        viewPager.adapter = TradeInfoContainerViewPagerAdapter(childFragmentManager, requireActivity().lifecycle)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                TradeInfoContainerViewPagerAdapter.MY_TRADES_TAB_POSITION -> getString(R.string.trade_info_my_trades_tab_title)
                TradeInfoContainerViewPagerAdapter.ORDERS_TAB_POSITION -> getString(R.string.trade_info_open_orders_tab_title)
                TradeInfoContainerViewPagerAdapter.TRADE_INFO_TAB_POSITION -> getString(R.string.trade_info_statistic_tab_title)
                else -> throw RuntimeException("Illegal position of tab $position")
            }
        }.attach()
    }
}