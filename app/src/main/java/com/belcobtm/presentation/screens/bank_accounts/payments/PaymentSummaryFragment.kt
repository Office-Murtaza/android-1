package com.belcobtm.presentation.screens.bank_accounts.payments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentPaymentSummaryBinding
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem
import com.belcobtm.domain.bank_account.item.PaymentInstructionsDataItem
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.helper.ClipBoardHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.bank_accounts.details.BankAccountDetailsFragmentDirections
import com.belcobtm.presentation.tools.extensions.code
import com.belcobtm.presentation.tools.extensions.formatBalanceValue
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.extensions.toStringPercents
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import wallet.core.jni.CoinType

class PaymentSummaryFragment : BaseFragment<FragmentPaymentSummaryBinding>() {

    private val args by navArgs<PaymentSummaryFragmentArgs>()
    private val viewModel by viewModel<PaymentSummaryViewModel>()
    private val clipBoardHelper: ClipBoardHelper by inject()
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPaymentSummaryBinding =
        FragmentPaymentSummaryBinding.inflate(inflater, container, false)

    override fun FragmentPaymentSummaryBinding.initListeners() {
        confirmButtonView.setOnClickListener {
            if (args.paymentSummaryItem.paymentType == BankAccountPaymentType.BUY)
                viewModel.createPayment(
                    walletId = args.paymentSummaryItem.walletId,
                    accountType = args.paymentSummaryItem.bankAccountType,
                    accountId = args.paymentSummaryItem.accountId,
                    transferAmount = args.paymentSummaryItem.valueExchangeTo,
                    paymentAmount = args.paymentSummaryItem.valueExchangeFrom,
                    bankAccountId = args.paymentSummaryItem.bankAccountId,
                    networkFee = args.paymentSummaryItem.networkFee,
                    paymentInstructions = args.paymentSummaryItem.instructionsDataItem
                )
            else
                viewModel.createPayout(
                    walletId = args.paymentSummaryItem.walletId,
                    accountType = args.paymentSummaryItem.bankAccountType,
                    accountId = args.paymentSummaryItem.accountId,
                    transferAmount = args.paymentSummaryItem.valueExchangeFrom,
                    paymentAmount = args.paymentSummaryItem.valueExchangeTo,
                    walletAddress = args.paymentSummaryItem.walletAddress,
                    bankAccountId = args.paymentSummaryItem.bankAccountId,
                    networkFee = args.paymentSummaryItem.networkFee
                )
        }
        cancelButtonView.setOnClickListener {
            requireActivity().onBackPressed()
        }
        tvTrackingRefValue.setOnClickListener {
            copyToClipboard(tvTrackingRefValue.text.toString())
        }
    }

    override fun FragmentPaymentSummaryBinding.initObservers() {
        viewModel.bankAccountPaymentLiveData.observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<BankAccountPaymentListItem> -> showLoading()
                is LoadingData.Success<BankAccountPaymentListItem> -> {
                    showContent()
                    val dest = BankAccountDetailsFragmentDirections.toPaymentDetailsFragment(
                        loadingData.data,
                        args.paymentSummaryItem.instructionsDataItem
                    )
                    navigate(dest)
                }
                is LoadingData.Error<BankAccountPaymentListItem> -> {
                    showContent()
                    Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_LONG).show()
                }
                else -> {
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    override fun FragmentPaymentSummaryBinding.initViews() {

        val paymentSummary = args.paymentSummaryItem
        showBackButton(true)
        setToolbarTitle(getString(R.string.payment_summary_title))

        viewModel.getUsdcInfo()

        when (paymentSummary.paymentType) {
            BankAccountPaymentType.BUY -> {
                paymentTypeChip.text = getString(R.string.buy)
                paymentTypeChip.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.buy_text_color
                    )
                )
                exchangeValueFromView.text =
                    paymentSummary.valueExchangeFrom.formatBalanceValue(getString(R.string.usd_currency))
                exchangeValueToView.text =
                    paymentSummary.valueExchangeTo.formatBalanceValue(getString(R.string.usdc_currency))
                tvNetworkFeeValue.text =
                    paymentSummary.networkFee.formatBalanceValue(getString(R.string.usdc_currency))
                tvPlatformFeeValue.text =
                    "${paymentSummary.platformFeePercent}% ~ ${paymentSummary.platformFeeValue.toStringCoin()} USDC"
            }
            BankAccountPaymentType.SELL -> {
                paymentTypeChip.text = getString(R.string.sell)
                paymentTypeChip.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.sell_text_color
                    )
                )
                exchangeValueToView.text =
                    paymentSummary.valueExchangeTo.formatBalanceValue(getString(R.string.usd_currency))
                exchangeValueFromView.text =
                    paymentSummary.valueExchangeFrom.formatBalanceValue(getString(R.string.usdc_currency))
                tvNetworkFeeValue.text =
                    paymentSummary.networkFee.formatBalanceValue(CoinType.ETHEREUM.code())
                tvPlatformFeeValue.text =
                    "${paymentSummary.platformFeePercent}% ~ $${paymentSummary.platformFeeValue.toStringPercents()}"
            }
            else -> {
            }
        }
        when (paymentSummary.bankAccountType) {
            BankAccountType.ACH -> {
                accountTypeChip.text = BankAccountType.ACH.stringValue
                accountTypeChip.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.chip_ach_text_color
                    )
                )
                wireInstructionsContainer.hide()
            }
            BankAccountType.WIRE -> {
                accountTypeChip.text = BankAccountType.WIRE.stringValue
                accountTypeChip.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.chip_wire_text_color
                    )
                )
                paymentSummary.instructionsDataItem?.let {
                    bindWireInstructionsView(it)
                } ?: run {
                    wireInstructionsContainer.hide()
                }
            }
        }

        tvProcessingTimeValue.text = paymentSummary.processingTime

    }

    private fun FragmentPaymentSummaryBinding.bindWireInstructionsView(data: PaymentInstructionsDataItem) {
        tvTrackingRefValue.text = data.trackingRef
        wireInstructionsBeneficiaryName.text = data.beneficiaryName
        wireInstructionsBeneficiaryAddress1.text = data.beneficiaryAddress1
        wireInstructionsBeneficiaryAddress2.text = data.beneficiaryAddress2
        wireInstructionsBeneficiaryBankName.text = data.beneficiaryBankName
        wireInstructionsBeneficiaryBankAddress.text = data.beneficiaryBankAddress
        wireInstructionsBeneficiaryBankCity.text = data.beneficiaryBankCity
        wireInstructionsBeneficiaryBankPostalCode.text = data.beneficiaryBankPostalCode
        wireInstructionsBeneficiaryBankCountry.text = data.beneficiaryBankCountry
        wireInstructionsBeneficiaryBankSwiftCode.text = data.beneficiaryBankSwiftCode
        wireInstructionsBeneficiaryBankRoutingNumber.text = data.beneficiaryBankRoutingNumber
        wireInstructionsBeneficiaryBankAccountNumber.text = data.beneficiaryAccountNumber
    }

    private fun copyToClipboard(copiedText: String) {
        clipBoardHelper.setTextToClipboard(copiedText)
        AlertHelper.showToastShort(requireContext(), R.string.copied)
    }

}
