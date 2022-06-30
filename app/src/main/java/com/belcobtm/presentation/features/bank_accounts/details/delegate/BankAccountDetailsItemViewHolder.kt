package com.belcobtm.presentation.features.bank_accounts.details.delegate

import androidx.core.content.ContextCompat
import com.belcobtm.R
import com.belcobtm.databinding.ItemBankAccountDetailsBuySellBinding
import com.belcobtm.domain.bank_account.item.BankAccountDetailsListItem
import com.belcobtm.domain.bank_account.type.BankAccountStatusType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.tools.extensions.formatBalanceValue
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.show
import com.belcobtm.presentation.tools.extensions.toggle

class BankAccountDetailsItemViewHolder(
    private val binding: ItemBankAccountDetailsBuySellBinding,
    onBankAccountDetailsClicked: (Boolean) -> Unit,
    onBuyClicked: () -> Unit,
    onSellClicked: () -> Unit,
) : MultiTypeViewHolder<BankAccountDetailsListItem>(binding.root) {
    init {
        binding.detailsButton.setOnClickListener {
            onBankAccountDetailsClicked(!model.isExpanded)
        }
        binding.buyButton.setOnClickListener { onBuyClicked() }
        binding.sellButton.setOnClickListener { onSellClicked() }
    }

    override fun bind(model: BankAccountDetailsListItem) {
        with(binding) {
            if (model.isExpanded) {
                detailsButton.setCompoundDrawablesWithIntrinsicBounds(
                    null, null,
                    ContextCompat.getDrawable(binding.root.context, R.drawable.ic_chevron_up), null
                )
                detailsContainer.root.show()
            } else {
                detailsButton.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(binding.root.context, R.drawable.ic_chevron_down),
                    null
                )
                detailsContainer.root.hide()
            }

            accountNameView.text = model.accountName
            balanceValueView.text = model.balanceValue?.formatBalanceValue(model.balanceCurrency)
            statusView.text = model.status.stringValue
            statusView.setCompoundDrawablesWithIntrinsicBounds(
                null, null,
                ContextCompat.getDrawable(binding.root.context, model.status.iconResource), null
            )
            if (model.status != BankAccountStatusType.COMPLETE) {
                buyButton.isEnabled = false
                buyButton.alpha = 0.2f
                sellButton.isEnabled = false
                sellButton.alpha = 0.2f
            }
            achTypeChip.toggle(model.bankAccountTypes.contains(BankAccountType.ACH))
            wireTypeChip.toggle(model.bankAccountTypes.contains(BankAccountType.WIRE))

            with(binding.detailsContainer) {
                if (model.billingDetails.name.isNotEmpty())
                    billingDetailsName.text = model.billingDetails.name
                else
                    billingDetailsNameContainer.hide()

                if (model.billingDetails.email.isNotEmpty())
                    billingDetailsEmail.text = model.billingDetails.email
                else
                    billingDetailsEmailContainer.hide()

                if (model.billingDetails.phone.isNotEmpty())
                    billingDetailsPhone.text = model.billingDetails.phone
                else
                    billingDetailsPhoneContainer.hide()

                if (model.billingDetails.country.isNotEmpty())
                    billingDetailsCountry.text = model.billingDetails.country
                else
                    billingDetailsCountryContainer.hide()

                if (model.billingDetails.region.isNotEmpty())
                    billingDetailsRegion.text = model.billingDetails.region
                else
                    billingDetailsRegionContainer.hide()

                if (model.billingDetails.city.isNotEmpty())
                    billingDetailsCity.text = model.billingDetails.city
                else
                    billingDetailsCityContainer.hide()

                if (model.billingDetails.street.isNotEmpty())
                    billingDetailsStreet.text = model.billingDetails.street
                else
                    billingDetailsStreetContainer.hide()

                if (model.billingDetails.postalCode.isNotEmpty())
                    billingDetailsPostalCode.text = model.billingDetails.postalCode
                else
                    billingDetailsPostalCodeContainer.hide()

                if (model.accountDetails.accountNumber.isNotEmpty())
                    accountDetailsAccountNumber.text = model.accountDetails.accountNumber
                else
                    accountDetailsAccountNumberContainer.hide()

                if (model.accountDetails.routingNumber.isNotEmpty())
                    accountDetailsRoutingNumber.text = model.accountDetails.routingNumber
                else
                    accountDetailsRoutingNumberContainer.hide()

                if (model.accountDetails.iban.isNotEmpty())
                    accountDetailsIban.text = model.accountDetails.iban
                else
                    accountDetailsIbanContainer.hide()

                if (model.plaidDetails.accountId.isNotEmpty())
                    plaidDetailsAccountId.text = model.plaidDetails.accountId
                else
                    plaidDetailsAccountIdContainer.hide()

                if (model.plaidDetails.type.isNotEmpty())
                    plaidDetailsType.text = model.plaidDetails.type
                else
                    plaidDetailsTypeContainer.hide()

                if (model.plaidDetails.subtype.isNotEmpty())
                    plaidDetailsSubtype.text = model.plaidDetails.subtype
                else
                    plaidDetailsSubtypeContainer.hide()

                if (model.plaidDetails.achNumber.isNotEmpty())
                    plaidDetailsAchNumber.text = model.plaidDetails.achNumber
                else
                    plaidDetailsAchNumberContainer.hide()

                if (model.plaidDetails.routingNumber.isNotEmpty())
                    plaidDetailsRoutingNumber.text = model.plaidDetails.routingNumber
                else
                    plaidDetailsRoutingNumberContainer.hide()

                if (model.plaidDetails.wireRouting.isNotEmpty())
                    plaidDetailsWireRouting.text = model.plaidDetails.wireRouting
                else
                    plaidDetailsWireRoutingContainer.hide()

                if (model.circleDetails.achAccountId.isNotEmpty())
                    circleDetailsAchAccountId.text = model.circleDetails.achAccountId
                else
                    circleDetailsAchAccountIdContainer.hide()

                if (model.circleDetails.wireAccountId.isNotEmpty())
                    circleDetailsWireAccountId.text = model.circleDetails.wireAccountId
                else
                    circleDetailsWireAccountIdContainer.hide()

                if (model.isAccountDetailsEmpty())
                    accountDetailsContainer.hide()
                if (model.isBillingDetailsEmpty())
                    billingDetailsContainer.hide()
                if (model.isCircleDetailsEmpty())
                    circleDetailsContainer.hide()
                if (model.isPlaidDetailsEmpty())
                    plaidDetailsContainer.hide()

                bankAccountDetailsDate.text = model.date

            }

        }
    }
}