package com.belcobtm.presentation.features.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import com.belcobtm.R
import com.belcobtm.databinding.FragmentSecurityBinding
import com.belcobtm.presentation.core.extensions.toggle
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.sms.code.SmsCodeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecurityFragment : BaseFragment<FragmentSecurityBinding>() {

    private val viewModel by viewModel<SecurityViewModel>()

    override var isBackButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(SmsCodeFragment.REQUEST_KEY) { requestKey, bundle ->
            if (bundle.getBoolean(SmsCodeFragment.BUNDLE_KEY_PHONE_UPDATE_VERIFICATION)) {
                viewModel.updatePhone()
            }
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSecurityBinding =
        FragmentSecurityBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.security_label)
        setClickListeners()
        observeData()
    }

    private fun setClickListeners() {
        binding.updatePhoneItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.PHONE) }
        binding.updatePassItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.PASS) }
        binding.updatePinItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.PIN) }
        binding.seedPhraseItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.SEED) }
        binding.unlinkItem.setOnClickListener { viewModel.handleItemClick(SecurityItem.UNLINK) }
        binding.switchBioAuthItem.setOnClickListener { viewModel.invertBioAuth() }
    }

    private fun observeData() {
        viewModel.userPhone.listen(success = { binding.updatePhoneItem.setValue(it) })
        viewModel.actionData.observe(viewLifecycleOwner) { action ->
            when (action) {
                is SecurityAction.NavigateAction -> navigate(action.navDirections)
                SecurityAction.PhoneSuccessfullyUpdated -> showPhoneUpdatedSuccess()
                SecurityAction.PhoneUpdateFailed -> showPhoneUpdatedFail()
            }
        }
        viewModel.bioOption.observe(viewLifecycleOwner) { bioOtion ->
            with(binding.switchBioAuthItem) {
                toggle(bioOtion.supported)
                setSwitchState(bioOtion.allowed)
            }
        }
    }

    private fun showPhoneUpdatedSuccess() {
        showToast(getString(R.string.update_phone_success))
    }

    private fun showPhoneUpdatedFail() {
        showToast(getString(R.string.update_phone_fail))
    }

}
