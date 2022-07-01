package com.belcobtm.presentation.screens.services

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
import com.belcobtm.presentation.screens.services.adapter.ServicesItemDelegate
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ServicesFragment : BaseFragment<FragmentDealsBinding>() {

    override var isMenuEnabled: Boolean = true

    private val availabilityProvider by inject<ServiceInfoProvider>()

    private val viewModel by viewModel<ServicesViewModel>()

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(ServicesItemDelegate(::onServiceClicked, ::onVerifyClicked))
        }
    }

    override fun FragmentDealsBinding.initObservers() {
        viewModel.apply {
            stateData.observe(viewLifecycleOwner) {
                it.commonData?.let { status ->

                }
            }
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
                navigate(ServicesFragmentDirections.toContactListFragment())
            ServiceType.TRADE ->
                navigate(ServicesFragmentDirections.toTradeContainerFragment())
            ServiceType.SWAP ->
                navigate(ServicesFragmentDirections.toSwapFragment())
            ServiceType.STAKING ->
                navigate(ServicesFragmentDirections.toStakingFragment())
            ServiceType.ATM_SELL ->
                navigate(ServicesFragmentDirections.toAtmSellFragment())
        }
    }

    private fun onVerifyClicked() {
        navigate(ServicesFragmentDirections.toVerificationInfoFragment())
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDealsBinding = FragmentDealsBinding.inflate(inflater, container, false)

}
