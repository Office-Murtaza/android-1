package com.app.belcobtm.presentation.features.wallet.trade.container

import android.os.Bundle
import android.view.*
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTradeListContainerBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.container.adapter.TradeContainerViewPagerAdapter
import com.app.belcobtm.presentation.features.wallet.trade.container.adapter.TradeContainerViewPagerAdapter.Companion.BUY_TRADES_TAB_POSITION
import com.app.belcobtm.presentation.features.wallet.trade.container.adapter.TradeContainerViewPagerAdapter.Companion.SELL_TRADES_TAB_POSITION
import com.app.belcobtm.presentation.features.wallet.trade.container.adapter.TradeContainerViewPagerAdapter.Companion.TRADE_INFO_TAB_POSITION
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.viewmodel.ext.android.viewModel

class TradeContainerFragment : BaseFragment<FragmentTradeListContainerBinding>() {

    override var isMenuEnabled: Boolean = true
    override var isHomeButtonEnabled: Boolean = true

    override val retryListener: View.OnClickListener = View.OnClickListener {
        viewModel.retry()
    }

    private val viewModel by viewModel<TradeContainerViewModel>()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeListContainerBinding =
        FragmentTradeListContainerBinding.inflate(inflater, container, false)

    override fun FragmentTradeListContainerBinding.initViews() {
        setToolbarTitle(R.string.trade_list_screen_title)
        viewPager.adapter = TradeContainerViewPagerAdapter(childFragmentManager, requireActivity().lifecycle)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                BUY_TRADES_TAB_POSITION -> getString(R.string.trade_list_buy_tab_title)
                SELL_TRADES_TAB_POSITION -> getString(R.string.trade_list_sell_tab_title)
                TRADE_INFO_TAB_POSITION -> getString(R.string.trade_list_my_info_tab_title)
                else -> throw RuntimeException("Illegal position of tab $position")
            }
        }.attach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchTrades()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.trade_menu, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.create_trade_menu_item -> {
            navigate(TradeContainerFragmentDirections.toCreateTradeFragment())
            true
        }
        else -> false
    }

    override fun FragmentTradeListContainerBinding.initObservers() {
        viewModel.loadingData.listen(error = {
            when (it) {
                is Failure.NetworkConnection -> showErrorNoInternetConnection()
                is Failure.MessageError -> {
                    showSnackBar(it.message.orEmpty())
                    showContent()
                }
                is Failure.ServerError -> showErrorServerError()
                else -> showErrorSomethingWrong()
            }
        })
    }
}