package com.belcobtm.presentation.features.bank_accounts.create

import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentBankAccountCreateBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.bank_account.item.BankAccountCreateDataItem
import com.belcobtm.domain.bank_account.item.BankAccountValidationErrorDataItem
import com.belcobtm.domain.bank_account.type.CreateBankAccountType
import com.belcobtm.presentation.core.extensions.getString
import com.belcobtm.presentation.core.extensions.hide
import com.belcobtm.presentation.core.extensions.setText
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class BankAccountCreateFragment : BaseFragment<FragmentBankAccountCreateBinding>() {

    private val args by navArgs<BankAccountCreateFragmentArgs>()
    val viewModel by viewModel<BankAccountCreateViewModel>()
    private var firstInvalidView: View? = null
    private var validated = false

    override val isBackButtonEnabled: Boolean = true

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBankAccountCreateBinding =
        FragmentBankAccountCreateBinding.inflate(inflater, container, false)

    override fun FragmentBankAccountCreateBinding.initViews() {
        if (args.createBankAccountType != CreateBankAccountType.NONE) {
            viewModel.selectedCreateBankAccountType = args.createBankAccountType
        }

        accountNumberView.showHelpText(getString(R.string.bank_account_field_account_no_helper_text))
        routingNumberView.showHelpText(getString(R.string.bank_account_field_routing_no_helper_text))
        ibanView.showHelpText(getString(R.string.bank_account_field_iban_helper_text))
        nameView.showHelpText(getString(R.string.bank_account_field_name_helper_text))
        countryView.showHelpText(getString(R.string.bank_account_field_country_helper_text))
        provinceView.showHelpText(getString(R.string.bank_account_field_province_helper_text))
        cityView.showHelpText(getString(R.string.bank_account_field_city_helper_text))
        addressView.showHelpText(getString(R.string.bank_account_field_address_helper_text))
        zipCodeView.showHelpText(getString(R.string.bank_account_field_zip_code_helper_text))
        bankNameView.showHelpText(getString(R.string.bank_account_field_bank_name_helper_text))
        bankCountryView.showHelpText(getString(R.string.bank_account_field_bank_country_helper_text))
        bankCityView.showHelpText(getString(R.string.bank_account_field_bank_city_helper_text))
        bankAddressTitle.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black_text_color
            )
        )

        when (viewModel.selectedCreateBankAccountType) {
            CreateBankAccountType.US -> {
                setToolbarTitle(getString(R.string.bank_account_select_type_screen_us_type))
                ibanView.hide()
                //  countryView.hide()
                bankNameView.hide()
                bankCityView.hide()
                countryView.setText("US")
                countryView.hideHelpText()
                countryView.hide()
                bankCountryView.setText("US")
                bankCountryView.hideHelpText()
                bankCountryView.isEnabled = false

                bankAddressTitle.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.disabled_text_color
                    )
                )
            }
            CreateBankAccountType.NON_US_IBAN -> {
                setToolbarTitle(getString(R.string.bank_account_select_type_screen_non_us_iban_type))
                accountNumberView.hide()
                routingNumberView.hide()
                bankNameView.hide()
            }
            CreateBankAccountType.NON_US_NON_IBAN -> {
                setToolbarTitle(getString(R.string.bank_account_select_type_screen_non_us_non_iban_type))
                ibanView.hide()
            }
            else -> {
            }
        }
        setUsdcTermsTextView()
    }

    private fun setUsdcTermsTextView() {
        val mainText = getString(R.string.bank_create_usdc_terms_text)
        val clickableText = getString(R.string.bank_usdc_terms_clickable)

        binding.termsTextView.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = SpannableStringBuilder()
                .append(mainText)
                .append(clickableText).apply {
                    setSpan(
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                openUsdcTermsLink()
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                ds.isUnderlineText = false
                                ds.color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                            }
                        },
                        mainText.length,
                        mainText.length + clickableText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
        }
    }

    private fun openUsdcTermsLink() {
        runCatching {
            activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(USDC_TERMS_LINK)).apply {
                putExtra(Browser.EXTRA_APPLICATION_ID, requireActivity().packageName)
            })
        }
    }

    override fun FragmentBankAccountCreateBinding.initListeners() {
        submitButton.setOnClickListener {
            validated = true
            if (isValidFields()) {
                viewModel.onCreateBankAccountSubmit(
                    BankAccountCreateDataItem(
                        accountNumber = if (accountNumberView.getString()
                                .isNotEmpty()
                        ) accountNumberView.getString() else null,
                        routingNumber = if (routingNumberView.getString()
                                .isNotEmpty()
                        ) routingNumberView.getString() else null,
                        iban = if (ibanView.getString()
                                .isNotEmpty()
                        ) ibanView.getString() else null,
                        name = if (nameView.getString()
                                .isNotEmpty()
                        ) nameView.getString() else null,
                        country = if (countryView.getString()
                                .isNotEmpty()
                        ) countryView.getString() else null,
                        province = if (provinceView.getString()
                                .isNotEmpty()
                        ) provinceView.getString() else null,
                        city = if (cityView.getString()
                                .isNotEmpty()
                        ) cityView.getString() else null,
                        address = if (addressView.getString()
                                .isNotEmpty()
                        ) addressView.getString() else null,
                        zipCode = if (zipCodeView.getString()
                                .isNotEmpty()
                        ) zipCodeView.getString() else null,
                        bankName = if (bankNameView.getString()
                                .isNotEmpty()
                        ) bankNameView.getString() else null,
                        bankCountry = if (bankCountryView.getString()
                                .isNotEmpty()
                        ) bankCountryView.getString() else null,
                        bankCity = if (bankCityView.getString()
                                .isNotEmpty()
                        ) bankCityView.getString() else null,
                    )
                )

            } else {
                firstInvalidView?.let {
                    bankAccountFieldsContainer.smoothScrollTo(0, it.top)
                }
            }
        }

        accountNumberView.editText?.addTextChangedListener {
            if (validated) {
                validateAccountNo()
            }
        }
        routingNumberView.editText?.addTextChangedListener {
            if (validated) {
                validateRoutingNo()
            }
        }
        ibanView.editText?.addTextChangedListener {
            if (validated) {
                validateIban()
            }
        }
        nameView.editText?.addTextChangedListener {
            if (validated) {
                validateName()
            }
        }
        countryView.editText?.addTextChangedListener {
            if (validated) {
                validateCountry()
            }
        }
        provinceView.editText?.addTextChangedListener {
            if (validated) {
                validateProvince()
            }
        }
        cityView.editText?.addTextChangedListener {
            if (validated) {
                validateCity()
            }
        }
        addressView.editText?.addTextChangedListener {
            if (validated) {
                validateAddress()
            }
        }
        zipCodeView.editText?.addTextChangedListener {
            if (validated) {
                validateZipCode()
            }
        }
        bankNameView.editText?.addTextChangedListener {
            if (validated) {
                validateBankName()
            }
        }
        bankCountryView.editText?.addTextChangedListener {
            if (validated) {
                validateBankCountry()
            }
        }
        bankCityView.editText?.addTextChangedListener {
            if (validated) {
                validateBankCity()
            }
        }
    }

    override fun FragmentBankAccountCreateBinding.initObservers() {
        viewModel.createBankAccountLiveData.observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading -> showLoading()
                is LoadingData.Success -> {
                    showContent()
                    if (loadingData.data.bankAccount != null && loadingData.data.validationError == null) {
                        popBackStack(R.id.bank_accounts_fragment, false)

                    } else if (loadingData.data.validationError != null) {
                        populateUiWithValidationErrors(loadingData.data.validationError)
                        firstInvalidView?.let {
                            binding.bankAccountFieldsContainer.post {
                                binding.bankAccountFieldsContainer.smoothScrollTo(0, it.top)
                            }
                        }
                    }
                }
                is LoadingData.Error -> {
                    when (loadingData.errorType) {
                        is Failure.MessageError -> showError(
                            loadingData.errorType.message
                                ?: getString(R.string.error_something_went_wrong)
                        )
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                }
            }
        }
    }

    private fun populateUiWithValidationErrors(errors: BankAccountValidationErrorDataItem) {
        with(errors) {
            if (bankCityValidationError != null)
                binding.bankCityView.showErrorText(bankCityValidationError!!)
            if (bankCountryValidationError != null)
                binding.bankCountryView.showErrorText(bankCountryValidationError!!)
            if (bankNameValidationError != null)
                binding.bankNameView.showErrorText(bankNameValidationError!!)
            if (zipCodeValidationError != null)
                binding.zipCodeView.showErrorText(zipCodeValidationError!!)
            if (addressValidationError != null)
                binding.addressView.showErrorText(addressValidationError!!)
            if (cityValidationError != null)
                binding.cityView.showErrorText(cityValidationError!!)
            if (provinceValidationError != null)
                binding.provinceView.showErrorText(provinceValidationError!!)
            if (countryValidationError != null)
                binding.countryView.showErrorText(countryValidationError!!)
            if (nameValidationError != null)
                binding.nameView.showErrorText(nameValidationError!!)
            if (ibanValidationError != null)
                binding.ibanView.showErrorText(ibanValidationError!!)
            if (routingNumberValidationError != null)
                binding.routingNumberView.showErrorText(routingNumberValidationError!!)
            if (accountNumberValidationError != null)
                binding.accountNumberView.showErrorText(accountNumberValidationError!!)

            validated = true
        }
    }

    private fun isValidFields(): Boolean {
        //order this as opposed to the order they have on the scrollview ( for the firstInvalidView to work)
        val bankCity = binding.validateBankCity()
        val bankCountry = binding.validateBankCountry()
        val bankName = binding.validateBankName()
        val zipCode = binding.validateZipCode()
        val address = binding.validateAddress()
        val city = binding.validateCity()
        val province = binding.validateProvince()
        val country = binding.validateCountry()
        val name = binding.validateName()
        val iban = binding.validateIban()
        val routingNumber = binding.validateRoutingNo()
        val accountNumber = binding.validateAccountNo()


        return bankCity
            && bankCountry
            && bankName
            && zipCode
            && address
            && city
            && province
            && country
            && name
            && iban
            && routingNumber
            && accountNumber
    }

    private fun FragmentBankAccountCreateBinding.validateAccountNo(): Boolean {
        return if (accountNumberView.getString().isEmpty() && accountNumberView.isVisible) {
            accountNumberView.showErrorText(getString(R.string.bank_account_field_account_no_error_text))
            false
        } else {
            accountNumberView.showHelpText(getString(R.string.bank_account_field_account_no_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateRoutingNo(): Boolean {
        return if (routingNumberView.getString().isEmpty() && routingNumberView.isVisible) {
            routingNumberView.showErrorText(getString(R.string.bank_account_field_routing_no_error_text))
            false
        } else {
            routingNumberView.showHelpText(getString(R.string.bank_account_field_routing_no_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateIban(): Boolean {
        return if (ibanView.getString().isEmpty() && ibanView.isVisible) {
            ibanView.showErrorText(getString(R.string.bank_account_field_iban_error_text))
            false
        } else {
            ibanView.showHelpText(getString(R.string.bank_account_field_iban_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateName(): Boolean {
        return if (nameView.getString().isEmpty() && nameView.isVisible) {
            nameView.showErrorText(getString(R.string.bank_account_field_name_error_text))
            false
        } else {
            nameView.showHelpText(getString(R.string.bank_account_field_name_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateCountry(): Boolean {
        return if (countryView.getString().isEmpty() && countryView.isVisible) {
            countryView.showErrorText(getString(R.string.bank_account_field_country_error_text))
            false
        } else {
            countryView.showHelpText(getString(R.string.bank_account_field_country_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateProvince(): Boolean {
        return if (provinceView.getString().isEmpty() && provinceView.isVisible) {
            provinceView.showErrorText(getString(R.string.bank_account_field_province_error_text))
            false
        } else {
            provinceView.showHelpText(getString(R.string.bank_account_field_province_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateCity(): Boolean {
        return if (cityView.getString().isEmpty() && cityView.isVisible) {
            cityView.showErrorText(getString(R.string.bank_account_field_city_error_text))
            false
        } else {
            cityView.showHelpText(getString(R.string.bank_account_field_city_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateAddress(): Boolean {
        return if (addressView.getString().isEmpty() && addressView.isVisible) {
            addressView.showErrorText(getString(R.string.bank_account_field_address_error_text))
            false
        } else {
            addressView.showHelpText(getString(R.string.bank_account_field_address_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateZipCode(): Boolean {
        return if (zipCodeView.getString().isEmpty() && zipCodeView.isVisible) {
            zipCodeView.showErrorText(getString(R.string.bank_account_field_zip_code_error_text))
            false
        } else {
            zipCodeView.showHelpText(getString(R.string.bank_account_field_zip_code_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateBankName(): Boolean {
        return if (bankNameView.getString().isEmpty() && bankNameView.isVisible) {
            bankNameView.showErrorText(getString(R.string.bank_account_field_bank_name_error_text))
            false
        } else {
            bankNameView.showHelpText(getString(R.string.bank_account_field_bank_name_helper_text))
            true
        }
    }

    private fun FragmentBankAccountCreateBinding.validateBankCountry(): Boolean {
        return if (bankCountryView.getString().isEmpty() && bankCountryView.isVisible) {
            bankCountryView.showErrorText(getString(R.string.bank_account_field_bank_country_error_text))
            false
        } else if (bankCountryView.isEnabled) {
            bankCountryView.showHelpText(getString(R.string.bank_account_field_country_helper_text))
            true
        } else true
    }

    private fun FragmentBankAccountCreateBinding.validateBankCity(): Boolean {
        return if (bankCityView.getString().isEmpty() && bankCityView.isVisible) {
            bankCityView.showErrorText(getString(R.string.bank_account_field_bank_city_error_text))
            false
        } else {
            bankCityView.showHelpText(getString(R.string.bank_account_field_bank_city_helper_text))
            true
        }
    }

    private fun TextInputLayout.showErrorText(message: String) {
        isErrorEnabled = true
        isHelperTextEnabled = false
        error = message
        firstInvalidView = this

    }

    private fun TextInputLayout.showHelpText(message: String) {
        isHelperTextEnabled = true
        isErrorEnabled = false
        helperText = message
    }

    private fun TextInputLayout.hideHelpText() {
        isHelperTextEnabled = false
    }

    companion object {

        private const val USDC_TERMS_LINK = "https://www.circle.com/en/legal/usdc-terms"
    }

}
