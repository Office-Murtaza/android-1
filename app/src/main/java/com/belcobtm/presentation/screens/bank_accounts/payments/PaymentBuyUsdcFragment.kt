package com.belcobtm.presentation.screens.bank_accounts.payments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentPaymentBuyUsdcBinding
import com.belcobtm.domain.bank_account.item.BankAccountDataItem
import com.belcobtm.domain.bank_account.item.PaymentSummaryDataItem
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.tools.extensions.formatBalanceValue
import com.belcobtm.presentation.tools.extensions.getString
import com.belcobtm.presentation.tools.extensions.onTextChanged
import com.belcobtm.presentation.tools.extensions.setText
import com.belcobtm.presentation.tools.extensions.show
import com.belcobtm.presentation.tools.extensions.toStringPercents
import com.belcobtm.presentation.tools.extensions.toggle
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentBuyUsdcFragment : BaseFragment<FragmentPaymentBuyUsdcBinding>() {

    private val args by navArgs<PaymentBuyUsdcFragmentArgs>()
    private val viewModel by viewModel<PaymentBuyUsdcViewModel>()
    private var _selectedAccountType = BankAccountType.NONE
    private var _convertedValue: Double = 0.00
    private var _networkFee: Double = 0.00
    private var _platformFee: Double = 0.00
    private lateinit var bankAccount: BankAccountDataItem

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPaymentBuyUsdcBinding =
        FragmentPaymentBuyUsdcBinding.inflate(inflater, container, false)

    override fun FragmentPaymentBuyUsdcBinding.initViews() {
        showBackButton(true)
        bankAccount = args.bankAccountDataItem
        setToolbarTitle(getString(R.string.buy_usdc))
        bankAccount.balanceValue?.let {
            tvBalanceValue.text = it.formatBalanceValue(getString(R.string.usd_currency))
            tvMax.show()
        } ?: run {
            tvBalanceValue.text = "-"
            tvMax.isInvisible = true
        }


        tvPlatformFeeValue.text = "${args.bankAccountInfo.feePercent}%"
        wireTypeChip.toggle(bankAccount.bankAccountTypes.indexOf(BankAccountType.WIRE) != -1)
        achTypeChip.toggle(bankAccount.bankAccountTypes.indexOf(BankAccountType.ACH) != -1)
        selectAccountType(bankAccount.bankAccountTypes.get(0))

    }

    override fun FragmentPaymentBuyUsdcBinding.initListeners() {
        achTypeChip.setOnClickListener {
            selectAccountType(BankAccountType.ACH)
        }
        wireTypeChip.setOnClickListener {
            selectAccountType(BankAccountType.WIRE)
        }
        amountInputLayout.editText?.onTextChanged {
            amountInputLayout.isErrorEnabled = false
            if (it.isNotEmpty()) {
                viewModel.computeConvertedValue(
                    it.toDouble(),
                    args.bankAccountInfo.feePercent.toDouble()
                )
                viewModel.amount = it.toDouble()
            } else {
                viewModel.computeConvertedValue(0.00, args.bankAccountInfo.feePercent.toDouble())
                viewModel.amount = 0.00
            }
        }

        tvMax.setOnClickListener {
            bankAccount.balanceValue?.let {
                amountInputLayout.setText(it.toStringPercents())
                viewModel.computeConvertedValue(it, args.bankAccountInfo.feePercent.toDouble())
            }
        }

        submitButtonView.setOnClickListener {
            if (validateData()) {
                goToPaymentSummary(
                    PaymentSummaryDataItem(
                        paymentType = BankAccountPaymentType.BUY,
                        bankAccountId = bankAccount.accountId,
                        accountId = when (_selectedAccountType) {
                            BankAccountType.WIRE -> bankAccount.circleDetails.wireAccountId
                            BankAccountType.ACH -> bankAccount.circleDetails.achAccountId
                            else -> ""
                        },
                        bankAccountType = _selectedAccountType,
                        valueExchangeFrom = viewModel.amount,
                        valueExchangeTo = _convertedValue,
                        networkFee = _networkFee,
                        platformFeePercent = args.bankAccountInfo.feePercent,
                        platformFeeValue = _platformFee,
                        processingTime = "~ 2-4 Days",
                        trackingRef = bankAccount.circleDetails.trackingRef,
                        walletId = args.bankAccountInfo.walletId,
                        walletAddress = args.bankAccountInfo.walletAddress,
                        instructionsDataItem = args.bankAccountDataItem.paymentInstructions
                    )
                )
            }
        }
    }

    private fun goToPaymentSummary(paymentSummary: PaymentSummaryDataItem) {
        val dest = PaymentBuyUsdcFragmentDirections.toPaymentSummaryFragment(paymentSummary)
        navigate(dest)
    }

    private fun validateData(): Boolean {
        if (binding.amountInputLayout.getString().isEmpty()) {
            binding.amountInputLayout.showErrorText(getString(R.string.payment_error_amount_empty))
            return false
        }
        if (viewModel.amount == 0.00) {
            binding.amountInputLayout.showErrorText(getString(R.string.payment_error_amount_zero))
            return false
        }
        if (viewModel.amount > bankAccount.balanceValue ?: 0.00 && _selectedAccountType != BankAccountType.WIRE) {
            binding.amountInputLayout.showErrorText(getString(R.string.payment_error_amount_exceeds_balance))
            return false
        }
        if (_convertedValue <= 0.00) {
            binding.amountInputLayout.showErrorText(getString(R.string.payment_error_converted_value_zero))
            return false
        }
        return true
    }

    override fun FragmentPaymentBuyUsdcBinding.initObservers() {

        viewModel.convertedValued.observe(viewLifecycleOwner) { convertedValue ->
            binding.tvConvertedValue.text =
                convertedValue.formatBalanceValue(getString(R.string.usdc_currency))
            _convertedValue = convertedValue
        }

        viewModel.platformFee.observe(viewLifecycleOwner) { platformFee ->
            binding.tvPlatformFeeValue.text =
                "${args.bankAccountInfo.feePercent}%, ${platformFee.formatBalanceValue(getString(R.string.usdc_currency))}"
            _platformFee = platformFee
        }

        viewModel.usdcExchangeValue.observe(viewLifecycleOwner) { usdcPrice ->
            tvExchangeRate.text =
                "1 USDC ~ ${usdcPrice.formatBalanceValue(getString(R.string.usd_currency))}"

        }

        viewModel.usdcNetworkFee.observe(viewLifecycleOwner) { networkFee ->
            tvNetworkFeeLabel.show()
            tvNetworkFeeValue.show()
            tvNetworkFeeValue.text =
                networkFee.formatBalanceValue(getString(R.string.usdc_currency))
            _networkFee = networkFee
            amountInputLayout.setText(viewModel.amount.toInt().toString())
        }
    }

    private fun FragmentPaymentBuyUsdcBinding.selectAccountType(type: BankAccountType) {
        _selectedAccountType = type
        when (type) {
            BankAccountType.ACH -> {
                achTypeChip.chipStrokeWidth = 7.00f
                wireTypeChip.chipStrokeWidth = 0.50f
                args.bankAccountInfo.limit.achLimit?.let {
                    tvTransactionLimitValue.text =
                        it.toDouble().formatBalanceValue(getString(R.string.usd_currency))
                    tvTransactionLimitValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text_color
                        )
                    )
                } ?: run {
                    tvTransactionLimitValue.text = getString(R.string.payment_limit_na)
                    tvTransactionLimitValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.gray_text_color
                        )
                    )
                }
            }
            BankAccountType.WIRE -> {
                achTypeChip.chipStrokeWidth = 0.50f
                wireTypeChip.chipStrokeWidth = 7.00f
                args.bankAccountInfo.limit.wireLimit?.let {
                    tvTransactionLimitValue.text = it.toDouble().formatBalanceValue(getString(R.string.usd_currency))
                    tvTransactionLimitValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text_color
                        )
                    )
                } ?: run {
                    tvTransactionLimitValue.text = getString(R.string.payment_limit_na)
                    tvTransactionLimitValue.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.gray_text_color
                        )
                    )
                }
            }
            else -> {
            }
        }
    }

    private fun TextInputLayout.showErrorText(message: String) {
        isErrorEnabled = true
        isHelperTextEnabled = false
        error = message
    }

}
