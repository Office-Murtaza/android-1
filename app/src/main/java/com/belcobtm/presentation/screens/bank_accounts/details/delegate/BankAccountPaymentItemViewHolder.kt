package com.belcobtm.presentation.screens.bank_accounts.details.delegate

import androidx.core.content.ContextCompat
import com.belcobtm.R
import com.belcobtm.databinding.ItemBankAccountTransactionBinding
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem
import com.belcobtm.domain.bank_account.type.BankAccountPaymentStatusType
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder

class BankAccountPaymentItemViewHolder(
    private val binding: ItemBankAccountTransactionBinding,
    onBankAccountPaymentClicked: (BankAccountPaymentListItem) -> Unit,
) : MultiTypeViewHolder<BankAccountPaymentListItem>(binding.root) {

    init {
        binding.root.setOnClickListener {
            onBankAccountPaymentClicked(model)
        }
    }

    override fun bind(model: BankAccountPaymentListItem) {
        with(binding) {
            if (model.paymentType == BankAccountPaymentType.BUY) {
                exchangeValueFromView.text =
                    binding.root.context.getString(R.string.usd_value_format, model.usdAmount)
                exchangeValueToView.text =
                    binding.root.context.getString(R.string.usdc_value_format, model.usdcAmount)
            } else {
                exchangeValueFromView.text =
                    binding.root.context.getString(R.string.usdc_value_format, model.usdcAmount)
                exchangeValueToView.text =
                    binding.root.context.getString(R.string.usd_value_format, model.usdAmount)
            }


            accountTypeView.apply {
                when (model.accountType) {
                    BankAccountType.ACH -> {
                        setText(R.string.ach_tag)
                        setTextColor(ContextCompat.getColor(context, R.color.chip_ach_text_color))
                    }
                    BankAccountType.WIRE -> {
                        setText(R.string.wire_tag)
                        setTextColor(ContextCompat.getColor(context, R.color.chip_wire_text_color))
                    }
                    else -> {
                    }
                }
            }

            val status: BankAccountPaymentStatusType

            if (model.paymentType == BankAccountPaymentType.BUY) {
                if (model.step == 1) {
                    transactionStepView.text =
                        binding.root.context.getString(R.string.step_1_usd_payment)
                    status = model.usdPaymentStatus
                } else {
                    transactionStepView.text =
                        binding.root.context.getString(R.string.step_2_usdc_transfer)
                    status = model.usdcTransferStatus
                }
            } else {
                if (model.step == 1) {
                    transactionStepView.text =
                        binding.root.context.getString(R.string.step_1_usdc_transfer)
                    status = model.usdcTransferStatus
                } else {
                    transactionStepView.text =
                        binding.root.context.getString(R.string.step_2_usd_payment)
                    status = model.usdPaymentStatus
                }
            }
            statusView.text = status.stringValue
            statusView.setCompoundDrawablesWithIntrinsicBounds(
                null, null,
                ContextCompat.getDrawable(binding.root.context, status.iconResource), null
            )

            dateView.text = model.date
        }
    }
}