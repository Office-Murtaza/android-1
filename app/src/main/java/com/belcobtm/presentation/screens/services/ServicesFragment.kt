package com.belcobtm.presentation.screens.services

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.belcobtm.R
import com.belcobtm.domain.service.ServiceType
import com.belcobtm.databinding.FragmentDealsBinding
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.services.adapter.ServicesItemDelegate
import org.koin.androidx.viewmodel.ext.android.viewModel

class ServicesFragment : BaseFragment<FragmentDealsBinding>() {

    override var isMenuEnabled: Boolean = true

    private val viewModel by viewModel<ServicesViewModel>()

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(ServicesItemDelegate(::onServiceClicked, ::onVerifyClicked))
        }
    }

    override fun FragmentDealsBinding.initObservers() {
        viewModel.apply {
            servicesLiveData.observe(viewLifecycleOwner, adapter::update)
        }
    }

    override fun FragmentDealsBinding.initViews() {
        setToolbarTitle(R.string.deals_toolsbar_title)

        val dividerItemDecoration = DividerItemDecoration(
            requireContext(), DividerItemDecoration.VERTICAL,
        )
        servicesList.adapter = adapter
        servicesList.addItemDecoration(dividerItemDecoration)
    }

    private fun onServiceClicked(item: ServiceItem) {
        navigate(
            when (item.serviceType) {
                ServiceType.TRANSFER ->
                    ServicesFragmentDirections.toContactListFragment()
                ServiceType.TRADE ->
                    ServicesFragmentDirections.toTradeContainerFragment()
                ServiceType.SWAP ->
                    ServicesFragmentDirections.toSwapFragment()
                ServiceType.STAKING ->
                    ServicesFragmentDirections.toStakingFragment()
                ServiceType.ATM_SELL ->
                    ServicesFragmentDirections.toAtmSellFragment()
            }
        )
    }

    private fun onVerifyClicked() {
        navigate(ServicesFragmentDirections.toVerificationInfoFragment())
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDealsBinding = FragmentDealsBinding.inflate(inflater, container, false)

}
