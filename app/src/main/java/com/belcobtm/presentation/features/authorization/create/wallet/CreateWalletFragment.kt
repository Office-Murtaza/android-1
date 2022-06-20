package com.belcobtm.presentation.features.authorization.create.wallet

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.belcobtm.R
import com.belcobtm.databinding.FragmentCreateWalletBinding
import com.belcobtm.domain.tools.openViewActivity
import com.belcobtm.presentation.core.Const
import com.belcobtm.presentation.core.extensions.afterTextChanged
import com.belcobtm.presentation.core.extensions.clearError
import com.belcobtm.presentation.core.extensions.clearText
import com.belcobtm.presentation.core.extensions.getString
import com.belcobtm.presentation.core.extensions.isEmail
import com.belcobtm.presentation.core.extensions.showError
import com.belcobtm.presentation.core.helper.SimpleClickableSpan
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.authorization.create.seed.CreateSeedFragment
import com.belcobtm.presentation.features.sms.code.SmsCodeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class CreateWalletFragment : BaseFragment<FragmentCreateWalletBinding>() {

    private val viewModel: CreateWalletViewModel by viewModel()
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        checkCredentialsWithPermissionCheck()
    }

    override fun FragmentCreateWalletBinding.initViews() {
        setToolbarTitle(R.string.create_wallet_screen_title)
        initTncView()
    }

    override fun FragmentCreateWalletBinding.initListeners() {
        nextButtonView.setOnClickListener { checkCredentialsWithPermissionCheck() }
        phoneView.editText?.afterTextChanged { updateNextButton() }
        passwordView.editText?.afterTextChanged { updateNextButton() }
        emailView.editText?.afterTextChanged { updateNextButton() }
        passwordConfirmView.editText?.afterTextChanged { updateNextButton() }
        tncCheckBoxView.setOnCheckedChangeListener { _, _ -> updateNextButton() }
        phoneEditView.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateWalletBinding =
        FragmentCreateWalletBinding.inflate(inflater, container, false)

    override fun FragmentCreateWalletBinding.initObservers() {
        viewModel.checkCredentialsLiveData.listen(
            success = {
                if (it.first) {
                    phoneView.showError(R.string.create_wallet_error_phone_registered)
                } else {
                    navigate(
                        R.id.to_sms_code_fragment,
                        bundleOf(
                            SmsCodeFragment.TAG_PHONE to getPhone(),
                            CreateSeedFragment.TAG_PASSWORD to passwordView.getString(),
                            CreateSeedFragment.TAG_EMAIL to emailView.getString(),
                            SmsCodeFragment.TAG_NEXT_FRAGMENT_ID to R.id.to_create_seed_fragment
                        )
                    )
                    tncCheckBoxView.isChecked = false
                    passwordView.clearText()
                    emailView.clearText()
                    passwordConfirmView.clearError()
                    viewModel.checkCredentialsLiveData.value = null
                }
            })
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
        binding.phoneView.clearError()
        binding.passwordView.clearError()
        binding.emailView.clearError()
        if (isValidFields()) {
            viewModel.checkCredentials(
                getPhone(),
                binding.passwordView.getString(),
                binding.emailView.getString()
            )
        }
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun showLocationRequiredErrorMessage() {
        showSnackBar(R.string.location_required_on_recover_or_register_validation_message)
    }

    private fun FragmentCreateWalletBinding.initTncView() {
        val linkText = getString(R.string.welcome_screen_terms_and_conditions)
        val linkClickableSpan = SimpleClickableSpan(
            onClick = { requireActivity().openViewActivity(Const.TERMS_URL, linkText) },
            updateDrawState = { it.isUnderlineText = false }
        )
        val defaultTextClickableSpan = SimpleClickableSpan(
            onClick = { tncCheckBoxView.isChecked = !tncCheckBoxView.isChecked },
            updateDrawState = {
                it.isUnderlineText = false
                it.color = ContextCompat.getColor(requireContext(), R.color.colorText)
            }
        )
        val fullText =
            SpannableStringBuilder(getString(R.string.welcome_screen_accept_terms_and_conditions))
        val startIndex = fullText.indexOf(linkText, 0, true)
        fullText.setSpan(
            linkClickableSpan,
            startIndex,
            fullText.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        fullText.setSpan(
            defaultTextClickableSpan,
            0,
            startIndex,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        tncTextView.text = TextUtils.expandTemplate(fullText, linkText)
        tncTextView.movementMethod = LinkMovementMethod.getInstance()
        tncTextView.highlightColor = Color.TRANSPARENT
    }

    private fun isValidFields(): Boolean {
        binding.passwordConfirmView.clearError()
        return when {
            isEmailInvalid() -> {
                binding.emailView.showError(R.string.recover_wallet_incorrect_email)
                false
            }
            binding.passwordView.getString().length < PASSWORD_MIN_LENGTH || binding.passwordView.getString().length > PASSWORD_MAX_LENGTH -> {
                binding.passwordConfirmView.showError(R.string.create_wallet_error_short_pass)
                false
            }
            binding.passwordView.getString() != binding.passwordConfirmView.getString() -> {
                binding.passwordConfirmView.showError(R.string.create_wallet_error_confirm_pass)
                false
            }
            else -> true
        }
    }

    private fun isEmailInvalid(): Boolean = binding.emailView.getString().isEmail().not()

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

    private fun updateNextButton() {
        binding.nextButtonView.isEnabled = binding.phoneView.getString().isNotEmpty()
            && viewModel.isValidMobileNumber(binding.phoneView.getString())
            && binding.emailView.getString().isNotEmpty()
            && binding.passwordView.getString().isNotEmpty()
            && binding.passwordConfirmView.getString().isNotEmpty()
            && binding.tncCheckBoxView.isChecked
    }

    private fun getPhone(): String = binding.phoneView.getString().replace("[-() ]".toRegex(), "")

    companion object {

        private const val PASSWORD_MIN_LENGTH: Int = 6
        private const val PASSWORD_MAX_LENGTH: Int = 20
    }

}
