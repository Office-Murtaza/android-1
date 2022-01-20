package com.belcobtm.presentation.features.deals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import com.afollestad.materialdialogs.MaterialDialog
import com.belcobtm.R
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.databinding.FragmentDealsBinding
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.domain.settings.type.isVerified
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.deals.delegate.DealsItemDelegate
import com.belcobtm.presentation.features.settings.SettingsFragmentDirections
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DealsFragment : BaseFragment<FragmentDealsBinding>() {
    override var isMenuEnabled: Boolean = true

    private val availabilityProvider by inject<ServiceInfoProvider>()

    private val viewModel by viewModel<DealsViewModel>()

    private lateinit var verifyDialog: MaterialDialog

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(DealsItemDelegate(::onServiceClicked))
        }
    }

    override fun FragmentDealsBinding.initObservers() {
        viewModel.apply {
            stateData.observe(viewLifecycleOwner, {
                it.commonData?.let { status ->
                    if (!status.isVerified()) {
                        verifyDialog.show()
                    }
                }
            })
        }
    }

    override fun FragmentDealsBinding.initViews() {
        setToolbarTitle(R.string.deals_toolsbar_title)
        verifyDialog = MaterialDialog(requireContext()).apply {
            cancelOnTouchOutside(false)
            cancelable(false)
            title(text = getString(R.string.settings_verify_dialog_title))
            message(text = getString(R.string.settings_verify_dialog_message))
            positiveButton(text = getString(R.string.settings_verify_dialog_verify)) {
                val dest = DealsFragmentDirections.toVerificationInfoFragment()
                navigate(dest)
            }
        }
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
