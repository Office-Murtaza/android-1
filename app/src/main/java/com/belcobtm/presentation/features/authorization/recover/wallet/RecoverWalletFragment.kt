package com.belcobtm.presentation.features.authorization.recover.wallet

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresPermission
import androidx.core.os.bundleOf
import com.belcobtm.R
import com.belcobtm.databinding.FragmentRecoverWalletBinding
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedFragment
import com.belcobtm.presentation.features.sms.code.SmsCodeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class RecoverWalletFragment : BaseFragment<FragmentRecoverWalletBinding>() {

    private val viewModel: RecoverWalletViewModel by viewModel()

    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        checkCredentialsWithPermissionCheck()
    }

    override fun FragmentRecoverWalletBinding.initViews() {
        setToolbarTitle(R.string.recover_wallet_screen_title)
    }

    override fun FragmentRecoverWalletBinding.initListeners() {
        nextButtonView.setOnClickListener {
            phoneView.clearError()
            passwordView.clearError()
            checkCredentialsWithPermissionCheck()
        }
        phoneView.editText?.afterTextChanged { updateNextButton() }
        passwordView.editText?.afterTextChanged { updateNextButton() }
        passwordView.editText?.actionDoneListener {
            hideKeyboard()
            checkCredentialsWithPermissionCheck()
        }
        phoneEditView.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRecoverWalletBinding =
        FragmentRecoverWalletBinding.inflate(inflater, container, false)

    override fun FragmentRecoverWalletBinding.initObservers() {
        viewModel.checkCredentialsLiveData.listen(success = {
            var isValid = true

            if (!it.first) {
                isValid = false
                phoneView.showError(R.string.recover_wallet_incorrect_login)
            } else {
                phoneView.clearError()
            }

            if (!it.second) {
                isValid = false
                passwordView.showError(R.string.recover_wallet_incorrect_password)
            } else {
                binding.passwordView.clearError()
            }

            if (isValid) {
                navigate(
                    R.id.to_sms_code_fragment,
                    bundleOf(
                        SmsCodeFragment.TAG_PHONE to getPhone(),
                        RecoverSeedFragment.TAG_PASSWORD to passwordView.getString(),
                        SmsCodeFragment.TAG_NEXT_FRAGMENT_ID to R.id.to_recover_seed_fragment
                    )
                )
                passwordView.clearText()
                viewModel.checkCredentialsLiveData.value = null
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.post {
            binding.phoneEditView.requestFocus()
            binding.phoneEditView.setSelection(
                binding.phoneEditView.length()
            )
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.showSoftInput(binding.phoneEditView, 0)
        }
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

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun checkCredentials() {
        val phone = getPhone()
        val password = binding.passwordView.getString()

        if (isValidFields(phone, password)) {
            viewModel.checkCredentials(phone, password)
        }
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun showLocationRequiredErrorMessage() {
        showSnackBar(R.string.location_required_on_recover_or_register_validation_message)
    }


    private fun isValidFields(phone: String, password: String): Boolean {
        val isEmptyFields = phone.isEmpty() || password.isEmpty()
        if (isEmptyFields) {
            showSnackBar(R.string.recover_wallet_error_all_fields_required)
        }

        return !isEmptyFields
    }

    private fun updateNextButton() {
        binding.nextButtonView.isEnabled = binding.phoneView.getString().isNotEmpty()
                && viewModel.isValidMobileNumber(binding.phoneView.getString())
                && binding.passwordView.getString().isNotEmpty()
    }

    private fun getPhone(): String = binding.phoneView.getString().replace("[-() ]".toRegex(), "")
}