package com.belcobtm.presentation.features.deals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import com.belcobtm.R
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.databinding.FragmentDealsBinding
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.deals.delegate.DealsItemDelegate
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject

class DealsFragment : BaseFragment<FragmentDealsBinding>() {
    override var isMenuEnabled: Boolean = true

    private val availabilityProvider by inject<ServiceInfoProvider>()

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(DealsItemDelegate(::onServiceClicked))
        }
    }

    override fun FragmentDealsBinding.initViews() {
        setToolbarTitle(R.string.deals_toolsbar_title)
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(), DividerItemDecoration.VERTICAL,
        )
        servicesList.adapter = adapter
        servicesList.addItemDecoration(dividerItemDecoration)
        availabilityProvider.observeServices()
            .asLiveData(Dispatchers.IO)
            .observe(viewLifecycleOwner, adapter::update)
    }

    private fun onServiceClicked(item: ServiceItem) {
        when (item.serviceType) {
            ServiceType.TRANSFER ->
                navigate(DealsFragmentDirections.toContactListFragment())
            ServiceType.TRADE ->
                navigate(DealsFragmentDirections.toTradeContainerFragment())
            ServiceType.SWAP ->
                navigate(DealsFragmentDirections.toSwapFragment())
            ServiceType.STAKING ->
                navigate(DealsFragmentDirections.toStakingFragment())
            ServiceType.ATM_SELL ->
                navigate(DealsFragmentDirections.toAtmSellFragment())
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDealsBinding = FragmentDealsBinding.inflate(inflater, container, false)

}
