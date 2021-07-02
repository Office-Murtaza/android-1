package com.app.belcobtm.presentation.features.wallet.trade.container

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTradeListContainerBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.container.adapter.TradeContainerViewPagerAdapter
import com.app.belcobtm.presentation.features.wallet.trade.container.adapter.TradeContainerViewPagerAdapter.Companion.BUY_TRADES_TAB_POSITION
import com.app.belcobtm.presentation.features.wallet.trade.container.adapter.TradeContainerViewPagerAdapter.Companion.SELL_TRADES_TAB_POSITION
import com.app.belcobtm.presentation.features.wallet.trade.container.adapter.TradeContainerViewPagerAdapter.Companion.TRADE_INFO_TAB_POSITION
import com.app.belcobtm.presentation.features.wallet.trade.info.TradeInfoContainerFragment
import com.app.belcobtm.presentation.features.wallet.trade.info.adapter.TradeInfoContainerViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class TradeContainerFragment : BaseFragment<FragmentTradeListContainerBinding>() {

    companion object {
        const val CREATE_TRADE_KEY = "create_trade_key"
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadWithDistanceCalculationWithPermissionCheck()
        viewModel.subscribeOnUpdates()
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.unsubscribeFromUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.trade_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.create_trade_menu_item -> {
            navigate(TradeContainerFragmentDirections.toCreateTradeFragment())
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun FragmentTradeListContainerBinding.initObservers() {
        getNavController()?.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>(CREATE_TRADE_KEY)?.observe(viewLifecycleOwner) {
                if (it) {
                    getNavController()?.currentBackStackEntry?.savedStateHandle?.set(CREATE_TRADE_KEY, false)
                    viewPager.postDelayed({
                        viewPager.currentItem = TRADE_INFO_TAB_POSITION
                        val userInfoFragment = childFragmentManager
                            .findFragmentByTag("f$TRADE_INFO_TAB_POSITION") as? TradeInfoContainerFragment
                        userInfoFragment?.preselectScreen(TradeInfoContainerViewPagerAdapter.MY_TRADES_TAB_POSITION)
                    }, 500)
                }
            }
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

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun loadWithDistanceCalculation() {
        viewModel.fetchTrades(calculateDistanceEnabled = true)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun loadWithoutDistanceCalculation() {
        viewModel.fetchTrades(calculateDistanceEnabled = false)
    }
}