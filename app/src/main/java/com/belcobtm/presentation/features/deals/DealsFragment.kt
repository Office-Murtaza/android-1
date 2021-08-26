package com.belcobtm.presentation.features.deals

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.databinding.FragmentDealsBinding
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.presentation.core.extensions.toggle
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.android.ext.android.inject

class DealsFragment : BaseFragment<FragmentDealsBinding>() {
    override var isMenuEnabled: Boolean = true

    private val availabilityProvider by inject<ServiceInfoProvider>()

    override fun FragmentDealsBinding.initViews() {
        setToolbarTitle(R.string.deals_toolsbar_title)
        val isSwapEnabled = availabilityProvider.isAvailableService(ServiceType.SWAP)
        swapItem.toggle(isSwapEnabled)
        swapItemDivider.toggle(isSwapEnabled)
        val isStakingEnabled = availabilityProvider.isAvailableService(ServiceType.STAKING)
        stakingItem.toggle(isStakingEnabled)
        stakingItemDivider.toggle(isStakingEnabled)
        val isTransferEnabled = availabilityProvider.isAvailableService(ServiceType.TRANSFER)
        transferItem.toggle(isTransferEnabled)
        transferItemDivider.toggle(isTransferEnabled)
        val isTradeEnabled = availabilityProvider.isAvailableService(ServiceType.TRADE)
        tradeItem.toggle(isTradeEnabled)
        tradeItemDivider.toggle(isTradeEnabled)
        val isAtmSellEnabled = availabilityProvider.isAvailableService(ServiceType.ATM_SELL)
        atmSellItem.toggle(isAtmSellEnabled)
        atmSellItemDivider.toggle(isAtmSellEnabled)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDealsBinding =
        FragmentDealsBinding.inflate(inflater, container, false)

    override fun FragmentDealsBinding.initListeners() {
        swapItem.setOnClickListener { navigate(DealsFragmentDirections.toSwapFragment()) }
        stakingItem.setOnClickListener { navigate(DealsFragmentDirections.toStakingFragment()) }
        transferItem.setOnClickListener { navigate(DealsFragmentDirections.toContactListFragment()) }
        tradeItem.setOnClickListener { navigate(DealsFragmentDirections.toTradeContainerFragment()) }
        atmSellItem.setOnClickListener { navigate(DealsFragmentDirections.toAtmSellFragment()) }
    }
}
