package com.app.belcobtm.presentation.features.authorization.create.wallet

import android.graphics.Color
import android.net.Uri
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.Const
import com.app.belcobtm.presentation.core.extensions.afterTextChanged
import com.app.belcobtm.presentation.core.extensions.clearError
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.extensions.showError
import com.app.belcobtm.presentation.core.helper.SimpleClickableSpan
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedFragment
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import kotlinx.android.synthetic.main.fragment_create_wallet.*
import org.koin.android.viewmodel.ext.android.viewModel


class CreateWalletFragment : BaseFragment() {
    private val viewModel: CreateWalletViewModel by viewModel()
    override val resourceLayout: Int = R.layout.fragment_create_wallet
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true

    override fun initViews() {
        setToolbarTitle(R.string.create_wallet_screen_title)
        initTncView()
    }

    override fun initListeners() {
        nextButtonView.setOnClickListener {
            phoneView.clearError()
            passwordView.clearError()
            if (isValidFields()) {
                viewModel.checkCredentials(getPhone(), passwordView.getString())
            }
        }
        phoneView.editText?.afterTextChanged { updateNextButton() }
        passwordView.editText?.afterTextChanged { updateNextButton() }
        passwordConfirmView.editText?.afterTextChanged { updateNextButton() }
        tncCheckBoxView.setOnCheckedChangeListener { _, _ -> updateNextButton() }

        phoneEditView.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    override fun initObservers() {
        viewModel.checkCredentialsLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress()
                is LoadingData.Success -> {
                    if (!it.data.first) {
                        navigate(
                            R.id.to_sms_code_fragment,
                            bundleOf(
                                SmsCodeFragment.TAG_PHONE to getPhone(),
                                CreateSeedFragment.TAG_PASSWORD to passwordView.getString(),
                                SmsCodeFragment.TAG_NEXT_FRAGMENT_ID to R.id.to_create_seed_fragment
                            )
                        )
                        viewModel.checkCredentialsLiveData.value = null
                    } else {
                        phoneView.showError(R.string.create_wallet_error_phone_registered)
                    }
                    showContent()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.IncorrectPassword -> passwordConfirmView.showError(R.string.recover_wallet_incorrect_password)
                        is Failure.MessageError -> showSnackBar(it.errorType.message)
                        is Failure.NetworkConnection -> showSnackBar(R.string.error_internet_unavailable)
                        else -> showSnackBar(R.string.error_something_went_wrong)
                    }
                    showContent()
                }
            }
        })
        viewModel.smsCodeLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress()
                is LoadingData.Success -> {
                    viewModel.checkCredentialsLiveData.value = null
                    viewModel.smsCodeLiveData.value = null
                    navigate(R.id.to_recover_seed_fragment)
                    showContent()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.MessageError -> showSnackBar(it.errorType.message)
                        is Failure.NetworkConnection -> showSnackBar(R.string.error_internet_unavailable)
                        else -> showSnackBar(R.string.error_something_went_wrong)
                    }
                    showContent()
                }
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

    private fun isValidFields(): Boolean = when {
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

    private fun updateNextButton() {
        nextButtonView.isEnabled =// getPhone().length == PHONE_LENGTH &&
            phoneView.getString().isNotEmpty()
                    && passwordView.getString().isNotEmpty()
                    && passwordConfirmView.getString().isNotEmpty()
                    && tncCheckBoxView.isChecked
    }

    private fun getPhone(): String = phoneView.getString().replace("[-() ]".toRegex(), "")

    companion object {
        private const val PASSWORD_MIN_LENGTH: Int = 6
        private const val PASSWORD_MAX_LENGTH: Int = 20
    }
}
