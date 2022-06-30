package com.belcobtm.presentation.screens.bank_accounts.payments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentPaymentSellUsdcBinding
import com.belcobtm.domain.bank_account.item.BankAccountDataItem
import com.belcobtm.domain.bank_account.item.PaymentSummaryDataItem
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.tools.extensions.code
import com.belcobtm.presentation.tools.extensions.formatBalanceValue
import com.belcobtm.presentation.tools.extensions.getString
import com.belcobtm.presentation.tools.extensions.onTextChanged
import com.belcobtm.presentation.tools.extensions.setText
import com.belcobtm.presentation.tools.extensions.show
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.extensions.toggle
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import wallet.core.jni.CoinType

class PaymentSellUsdcFragment : BaseFragment<FragmentPaymentSellUsdcBinding>() {

    private val args by navArgs<PaymentSellUsdcFragmentArgs>()
    private val viewModel by viewModel<PaymentSellUsdcViewModel>()
    private var _selectedAccountType = BankAccountType.NONE
    private var _convertedValue: Double = 0.00
    private var _networkFee: Double = 0.00
    private var _balance: Double = 0.00
    private var _platformFee: Double = 0.00
    private lateinit var bankAccount: BankAccountDataItem

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPaymentSellUsdcBinding =
        FragmentPaymentSellUsdcBinding.inflate(inflater, container, false)

    override fun FragmentPaymentSellUsdcBinding.initViews() {
        showBackButton(true)
        bankAccount = args.bankAccountDataItem
        setToolbarTitle(getString(R.string.sell_usdc))



        tvPlatformFeeValue.text = "${args.bankAccountInfo.feePercent}%"
        wireTypeChip.toggle(bankAccount.bankAccountTypes.indexOf(BankAccountType.WIRE) != -1)
        achTypeChip.toggle(bankAccount.bankAccountTypes.indexOf(BankAccountType.ACH) != -1)
        selectAccountType(bankAccount.bankAccountTypes.get(0))

    }

    override fun FragmentPaymentSellUsdcBinding.initListeners() {
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
            _balance.let {
                amountInputLayout.setText(it.toStringCoin())
                viewModel.computeConvertedValue(it, args.bankAccountInfo.feePercent.toDouble())
            }
        }
        submitButtonView.setOnClickListener {
            if (validateData()) {
                goToPaymentSummary(
                    PaymentSummaryDataItem(
                        paymentType = BankAccountPaymentType.SELL,
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
                        trackingRef = null,
                        walletId = args.bankAccountInfo.walletId,
                        walletAddress = args.bankAccountInfo.walletAddress,
                        bankAccountId = bankAccount.accountId,
                        instructionsDataItem = null
                    )
                )
            }
        }
    }

    override fun FragmentPaymentSellUsdcBinding.initObservers() {
        viewModel.convertedValued.observe(viewLifecycleOwner) { convertedValue ->
            binding.tvConvertedValue.text =
                convertedValue.formatBalanceValue(getString(R.string.usd_currency))
            _convertedValue = convertedValue
        }
        viewModel.platformFee.observe(viewLifecycleOwner) { platformFee ->
            binding.tvPlatformFeeValue.text =
                "${args.bankAccountInfo.feePercent}%, ${platformFee.formatBalanceValue(getString(R.string.usd_currency))}"
            _platformFee = platformFee
        }
        viewModel.usdcExchangeValue.observe(viewLifecycleOwner) { usdcPrice ->
            tvExchangeRate.text =
                "1 USDC ~ ${usdcPrice.formatBalanceValue(getString(R.string.usd_currency))}"
        }
        viewModel.usdcBalance.observe(viewLifecycleOwner) { usdcBalance ->
            tvBalanceValue.text = usdcBalance.formatBalanceValue(getString(R.string.usdc_currency))
            _balance = usdcBalance
            amountInputLayout.setText(viewModel.amount.toInt().toString())
        }
        viewModel.ethNetworkFee.observe(viewLifecycleOwner) { networkFee ->
            tvNetworkFeeLabel.show()
            tvNetworkFeeValue.show()
            tvNetworkFeeValue.text = networkFee.formatBalanceValue(CoinType.ETHEREUM.code())
            _networkFee = networkFee
        }

    }

    private fun FragmentPaymentSellUsdcBinding.selectAccountType(type: BankAccountType) {
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
            else -> {
            }
        }
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
        if (viewModel.amount > _balance) {
            binding.amountInputLayout.showErrorText(getString(R.string.payment_error_amount_exceeds_balance))
            return false
        }
        if (_networkFee > viewModel.ethDataItem.balanceCoin) {
            binding.amountInputLayout.showErrorText(getString(R.string.payment_error_fee_exceed_eth_balance))
            return false
        }
        return true
    }

    private fun goToPaymentSummary(paymentSummary: PaymentSummaryDataItem) {
        val dest = PaymentSellUsdcFragmentDirections.toPaymentSummaryFragment(paymentSummary)
        navigate(dest)
    }

    private fun TextInputLayout.showErrorText(message: String) {
        isErrorEnabled = true
        isHelperTextEnabled = false
        error = message
    }

}
