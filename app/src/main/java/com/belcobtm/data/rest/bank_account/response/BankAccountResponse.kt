package com.belcobtm.data.rest.bank_account.response

import com.belcobtm.domain.bank_account.item.*
import com.belcobtm.domain.bank_account.type.BankAccountStatusType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.DateFormat

data class BankAccountResponse(
    val id: String?,
    val userId: String?,
    val balance: Double?,
    val currency: String?,
    val bankName: String?,
    val accountName: String?,
    val status: String?,
    val accountTypes: List<String>?,
    val accountDetails: AccountDetails?,
    val plaid: PlaidDetails?,
    val bankAddress: BankAddress?,
    val billingDetails: BillingDetails?,
    val circle: CircleDetails?,
    val createdAt: Long?,
)

fun BankAccountResponse.mapToDataItem(): BankAccountDataItem =
    BankAccountDataItem(
        accountId = id ?: "",
        userId = userId ?: "",
        accountName = accountName ?: "",
        bankName = bankName ?: "",
        balanceValue = balance,
        currency = currency,
        bankAccountStatus = BankAccountStatusType.fromString(status),
        bankAccountTypes = accountTypes?.map { stringValue ->
            BankAccountType.fromString(stringValue)
        } ?: listOf(),
        billingDetails = BillingDetailsDataItem(
            name = billingDetails?.name ?: "",
            email = billingDetails?.email ?: "",
            phone = billingDetails?.phone ?: "",
            country = billingDetails?.country ?: "",
            region = billingDetails?.region ?: "",
            city = billingDetails?.city ?: "",
            street = billingDetails?.street ?: "",
            postalCode = billingDetails?.postalCode ?: ""
        ),
        plaidDetails = PlaidDetailsDataItem(
            accountId = plaid?.accountId ?: "",
            type = plaid?.type ?: "",
            subtype = plaid?.subtype ?: "",
            achNumber = plaid?.achNumber ?: "",
            routingNumber = plaid?.routingNumber ?: "",
            wireRouting = plaid?.wireRouting ?: "",
        ),
        accountDetails = AccountDetailsDataItem(
            iban = accountDetails?.iban ?: "",
            routingNumber = accountDetails?.routingNumber ?: "",
            accountNumber = accountDetails?.accountNumber ?: "",
        ),
        circleDetails = CircleDetailsDataItem(
            achAccountId = circle?.achAccountId ?: "",
            wireAccountId = circle?.wireAccountId ?: "",
            trackingRef = circle?.wirePaymentInstructions?.trackingRef ?: "",
        ),
        paymentInstructions = PaymentInstructionsDataItem(
            trackingRef = circle?.wirePaymentInstructions?.trackingRef ?: "",
            beneficiaryName = circle?.wirePaymentInstructions?.beneficiary?.name ?: "",
            beneficiaryAddress1 = circle?.wirePaymentInstructions?.beneficiary?.address1 ?: "",
            beneficiaryAddress2 = circle?.wirePaymentInstructions?.beneficiary?.address2 ?: "",
            beneficiaryBankName = circle?.wirePaymentInstructions?.beneficiaryBank?.name ?: "",
            beneficiaryBankSwiftCode = circle?.wirePaymentInstructions?.beneficiaryBank?.swiftCode
                ?: "",
            beneficiaryBankRoutingNumber = circle?.wirePaymentInstructions?.beneficiaryBank?.routingNumber
                ?: "",
            beneficiaryAccountNumber = circle?.wirePaymentInstructions?.beneficiaryBank?.accountNumber
                ?: "",
            beneficiaryBankAddress = circle?.wirePaymentInstructions?.beneficiaryBank?.address
                ?: "",
            beneficiaryBankCity = circle?.wirePaymentInstructions?.beneficiaryBank?.city ?: "",
            beneficiaryBankPostalCode = circle?.wirePaymentInstructions?.beneficiaryBank?.postalCode
                ?: "",
            beneficiaryBankCountry = circle?.wirePaymentInstructions?.beneficiaryBank?.country
                ?: "",
            virtualAccountEnabled = false,
        ),
        date = DateFormat.sdfLong.format(createdAt),
        createdAt = createdAt ?: 0,
    )

data class CircleDetails(
    val achAccountId: String?,
    val wireAccountId: String?,
    val wireFingerPrint: String?,
    val wirePaymentInstructions: WirePaymentInstructions?
)

data class WirePaymentInstructions(
    val trackingRef: String?,
    val beneficiary: InstructionsBeneficiary?,
    val beneficiaryBank: InstructionsBeneficiaryBank?,
)

data class PlaidDetails(
    val accountId: String?,
    val type: String?,
    val subtype: String?,
    val achNumber: String?,
    val routingNumber: String?,
    val wireRouting: String?,

    )

data class AccountDetails(
    val accountNumber: String?,
    val routingNumber: String?,
    val iban: String?,
)

data class BankAddress(
    val country: String?,
    val city: String?,
)

data class BillingDetails(
    val name: String?,
    val email: String?,
    val phone: String?,
    val region: String?,
    val country: String?,
    val city: String?,
    val street: String?,
    val postalCode: String?,
)


