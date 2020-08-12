package com.app.belcobtm.presentation.features.authorization.create.wallet

import android.graphics.Color
import android.net.Uri
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.Const
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.SimpleClickableSpan
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedFragment
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.android.synthetic.main.fragment_create_wallet.*
import org.koin.android.viewmodel.ext.android.viewModel

class CreateWalletFragment : BaseFragment() {
    private val viewModel: CreateWalletViewModel by viewModel()
    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.createInstance(requireContext()) }
    override val resourceLayout: Int = R.layout.fragment_create_wallet
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener { checkCredentials() }

    override fun initViews() {
        setToolbarTitle(R.string.create_wallet_screen_title)
        initTncView()
    }

    override fun initListeners() {
        nextButtonView.setOnClickListener { checkCredentials() }
        phoneView.editText?.afterTextChanged { updateNextButton() }
        passwordView.editText?.afterTextChanged { updateNextButton() }
        passwordConfirmView.editText?.afterTextChanged { updateNextButton() }
        tncCheckBoxView.setOnCheckedChangeListener { _, _ -> updateNextButton() }

        phoneEditView.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    override fun initObservers() {
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
                            SmsCodeFragment.TAG_NEXT_FRAGMENT_ID to R.id.to_create_seed_fragment
                        )
                    )
                    tncCheckBoxView.isChecked = false
                    passwordView.clearText()
                    passwordConfirmView.clearError()
                    viewModel.checkCredentialsLiveData.value = null
                }
            })
    }

    private fun initTncView() {
        val linkClickableSpan = SimpleClickableSpan(
            onClick = { CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(Const.TERMS_URL)) },
            updateDrawState = { it.isUnderlineText = false }
        )
        val defaultTextClickableSpan = SimpleClickableSpan(
            onClick = { tncCheckBoxView.isChecked = !tncCheckBoxView.isChecked },
            updateDrawState = {
                it.isUnderlineText = false
                it.color = ContextCompat.getColor(requireContext(), R.color.colorText)
            }
        )
        val linkText = getString(R.string.welcome_screen_terms_and_conditions)
        val fullText = SpannableString(getString(R.string.welcome_screen_accept_terms_and_conditions))
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
        passwordConfirmView.clearError()
        return when {
            passwordView.getString().length < PASSWORD_MIN_LENGTH || passwordView.getString().length > PASSWORD_MAX_LENGTH -> {
                passwordConfirmView.showError(R.string.create_wallet_error_short_pass)
                false
            }
            passwordView.getString() != passwordConfirmView.getString() -> {
                passwordConfirmView.showError(R.string.create_wallet_error_confirm_pass)
                false
            }
            else -> true
        }
    }

    private fun updateNextButton() {
        nextButtonView.isEnabled = phoneView.getString().isNotEmpty()
                && isValidMobileNumber(phoneView.getString())
                && passwordView.getString().isNotEmpty()
                && passwordConfirmView.getString().isNotEmpty()
                && tncCheckBoxView.isChecked
    }

    private fun isValidMobileNumber(phone: String): Boolean = if (phone.isNotBlank()) {
        try {
            val number = PhoneNumberUtil.createInstance(requireContext()).parse(phone, "")
            phoneUtil.isValidNumber(number)
        } catch (e: NumberParseException) {
            false
        }
    } else {
        false
    }

    private fun getPhone(): String = phoneView.getString().replace("[-() ]".toRegex(), "")

    private fun checkCredentials() {
        phoneView.clearError()
        passwordView.clearError()
        if (isValidFields()) {
            viewModel.checkCredentials(getPhone(), passwordView.getString())
        }
    }

    companion object {
        private const val PASSWORD_MIN_LENGTH: Int = 6
        private const val PASSWORD_MAX_LENGTH: Int = 20
    }
}
