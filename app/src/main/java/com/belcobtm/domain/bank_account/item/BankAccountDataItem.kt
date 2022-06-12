package com.belcobtm.domain.bank_account.item

import android.os.Parcelable
import com.belcobtm.domain.bank_account.type.BankAccountStatusType
import com.belcobtm.domain.bank_account.type.BankAccountType
import kotlinx.parcelize.Parcelize

@Parcelize
data class BankAccountDataItem(
    val accountId: String,
    val userId: String,
    val accountName: String,
    val bankName: String,
    val balanceValue: Double?,
    val currency: String?,
    val bankAccountStatus: BankAccountStatusType,
    val bankAccountTypes: List<BankAccountType>,
    val billingDetails: BillingDetailsDataItem,
    val accountDetails: AccountDetailsDataItem,
    val plaidDetails: PlaidDetailsDataItem,
    val circleDetails: CircleDetailsDataItem,
    val date: String,
    val createdAt: Long,
    val paymentInstructions: PaymentInstructionsDataItem,
) : Parcelable

fun BankAccountDataItem.toDetailsListItem(isExpanded: Boolean): BankAccountDetailsListItem =
    BankAccountDetailsListItem(
        id = accountId,
        bankName = bankName,
        accountName = accountName,
        balanceValue = balanceValue,
        balanceCurrency = currency,
        status = bankAccountStatus,
        bankAccountTypes = bankAccountTypes,
        billingDetails = billingDetails,
        plaidDetails = plaidDetails,
        accountDetails = accountDetails,
        circleDetails = circleDetails,
        date = date,
        isExpanded = isExpanded,
    )

fun BankAccountDataItem.toListItem(): BankAccountListItem =
    BankAccountListItem(
        id = accountId,
        accountName = accountName,
        bankName = bankName,
        balanceValue = balanceValue,
        balanceCurrency = currency,
        bankAccountTypes = bankAccountTypes,
        status = bankAccountStatus,
        billingDetails = billingDetails,
        plaidDetails = plaidDetails,
        circleDetails = circleDetails,
        accountDetails = accountDetails,
        date = date,
        createdAt = createdAt
    )


@Parcelize
data class BillingDetailsDataItem(
    val name: String,
    val email: String,
    val phone: String,
    val country: String,
    val region: String,
    val city: String,
    val street: String,
    val postalCode: String,
) : Parcelable

@Parcelize
data class PlaidDetailsDataItem(
    val accountId: String,
    val type: String,
    val subtype: String,
    val achNumber: String,
    val routingNumber: String,
    val wireRouting: String,
) : Parcelable

@Parcelize
data class AccountDetailsDataItem(
    val iban: String,
    val routingNumber: String,
    val accountNumber: String,
) : Parcelable

@Parcelize
data class CircleDetailsDataItem(
    val achAccountId: String,
    val wireAccountId: String,
    val trackingRef: String,
) : Parcelable

