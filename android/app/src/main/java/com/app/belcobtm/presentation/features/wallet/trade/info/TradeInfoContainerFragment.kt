package com.app.belcobtm.presentation.features.wallet.trade.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTradeInfoContainerBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.info.adapter.TradeInfoContainerViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class TradeInfoContainerFragment : BaseFragment<FragmentTradeInfoContainerBinding>() {

    override val isToolbarEnabled: Boolean
        get() = false

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeInfoContainerBinding =
        FragmentTradeInfoContainerBinding.inflate(inflater, container, false)

    override fun resolveNavController(view: View): NavController =
        requireParentFragment().findNavController()

    override fun FragmentTradeInfoContainerBinding.initViews() {
        viewPager.adapter = TradeInfoContainerViewPagerAdapter(parentFragmentManager, requireActivity().lifecycle)
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