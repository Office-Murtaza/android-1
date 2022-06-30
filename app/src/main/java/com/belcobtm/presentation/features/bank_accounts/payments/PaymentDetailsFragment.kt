package com.belcobtm.presentation.features.bank_accounts.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentPaymentDetailsBinding
import com.belcobtm.domain.bank_account.item.PaymentInstructionsDataItem
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.helper.ClipBoardHelper
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.tools.extensions.formatBalanceValue
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.toStringCoin
import org.koin.android.ext.android.inject

class PaymentDetailsFragment : BaseFragment<FragmentPaymentDetailsBinding>() {

    private val args by navArgs<PaymentDetailsFragmentArgs>()
    private val clipBoardHelper: ClipBoardHelper by inject()
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPaymentDetailsBinding =
        FragmentPaymentDetailsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                popBackStack(R.id.bank_account_details_fragment, false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            hideKeyboard()
            popBackStack(R.id.bank_account_details_fragment, false)
            true
        } else {
            false
        }

    override fun FragmentPaymentDetailsBinding.initViews() {

        showBackButton(true)
        setToolbarTitle(getString(R.string.payment_details_title))
        val payment = args.bankAccountPaymentListItem

        idView.text = payment.id

        tvTrackingRefValue.setOnClickListener {
            copyToClipboard(tvTrackingRefValue.text.toString())
        }
        copyIdIv.setOnClickListener {
            copyToClipboard(idView.text.toString())
        }
        step1CopyHashIv.setOnClickListener {
            copyToClipboard(step1HashView.text.toString())
        }
        step2CopyHashIv.setOnClickListener {
            copyToClipboard(step2HashView.text.toString())
        }
        dateView.text = payment.date
        feeView.text = "${payment.networkFee?.toStringCoin()} ${payment.networkFeeCurrency ?: ""}"
        when (payment.paymentType) {
            BankAccountPaymentType.BUY -> {
                paymentTypeChip.text = getString(R.string.buy)
                paymentTypeChip.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.buy_text_color
                    )
                )
                amountFirstValueView.text = getString(R.string.usd_value_format, payment.usdAmount)
                amountSecondValueView.text =
                    getString(R.string.usdc_value_format, payment.usdcAmount)

                platformFeeView.text =
                    payment.platformFee?.formatBalanceValue(getString(R.string.usdc_currency))

                firstStepStatusValue.text = payment.usdPaymentStatus.stringValue
                firstStepStatusValue.setCompoundDrawablesWithIntrinsicBounds(
                    null, null,
                    ContextCompat.getDrawable(
                        binding.root.context,
                        payment.usdPaymentStatus.iconResource
                    ), null
                )
                firstStepValue.text = getString(R.string.usd_payment)
                step1HashLabel.hide()
                step1HashView.hide()

                secondStepStatusValue.text = payment.usdcTransferStatus.stringValue
                secondStepStatusValue.setCompoundDrawablesWithIntrinsicBounds(
                    null, null,
                    ContextCompat.getDrawable(
                        binding.root.context,
                        payment.usdcTransferStatus.iconResource
                    ), null
                )
                secondStepValue.text = getString(R.string.usdc_transfer)
                step2HashView.text = payment.usdcTransferHash
                step1HashView.hide()
                step1HashLabel.hide()
                step1CopyHashIv.hide()
                step1CopyIvSeparator.hide()

                // tvNetworkFeeValue.text = paymentSummary.networkFee.toStringCoin() + " USDC"
            }
            BankAccountPaymentType.SELL -> {
                paymentTypeChip.text = getString(R.string.sell)
                paymentTypeChip.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.sell_text_color
                    )
                )
                amountSecondValueView.text = getString(R.string.usd_value_format, payment.usdAmount)
                amountFirstValueView.text =
                    getString(R.string.usdc_value_format, payment.usdcAmount)

                platformFeeView.text = payment.platformFee?.formatBalanceValue(getString(R.string.usdc_currency))

                //   tvNetworkFeeValue.text = paymentSummary.networkFee.toStringCoin() + " ETH"
                firstStepStatusValue.text = payment.usdcTransferStatus.stringValue
                firstStepStatusValue.setCompoundDrawablesWithIntrinsicBounds(
                    null, null,
                    ContextCompat.getDrawable(
                        binding.root.context,
                        payment.usdcTransferStatus.iconResource
                    ), null
                )
                step1HashView.text = payment.usdcTransferHash
                step2HashLabel.hide()
                step2HashView.hide()
                step2CopyHashIv.hide()
                step2CopyIvSeparator.hide()

                firstStepValue.text = getString(R.string.usdc_transfer)
                secondStepStatusValue.text = payment.usdPaymentStatus.stringValue
                secondStepStatusValue.setCompoundDrawablesWithIntrinsicBounds(
                    null, null,
                    ContextCompat.getDrawable(
                        binding.root.context,
                        payment.usdPaymentStatus.iconResource
                    ), null
                )
                secondStepValue.text = getString(R.string.usd_payment)

            }
            else -> {
            }
        }
        when (payment.accountType) {
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

                args.paymentInstructions?.let {
                    bindWireInstructionsView(it)
                }
            }
        }

        when (payment.step) {
            1 -> {
                ivFirstStep.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_step_1_completed
                    )
                )
                ivSecondStep.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_step_2
                    )
                )
            }
            2 -> {
                ivFirstStep.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_step_1_completed
                    )
                )
                ivSecondStep.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_step_2_completed
                    )
                )
            }
        }

    }

    private fun FragmentPaymentDetailsBinding.bindWireInstructionsView(data: PaymentInstructionsDataItem) {
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
