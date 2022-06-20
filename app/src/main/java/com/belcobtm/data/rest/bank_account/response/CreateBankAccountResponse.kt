package com.belcobtm.data.rest.bank_account.response

import com.belcobtm.domain.bank_account.item.*
import com.belcobtm.domain.bank_account.type.BankAccountStatusType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.DateFormat

data class CreateBankAccountResponse(
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
    val timestamp: Long?,
    val error: CreateBankAccountError?
)

fun CreateBankAccountResponse.toDataItem(): BankAccountCreateResponseDataItem =
    BankAccountCreateResponseDataItem(
        bankAccount = BankAccountDataItem(
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
            date = DateFormat.sdfLong.format(timestamp),
            timestamp = timestamp ?: 0
        )
    )


fun CreateBankAccountResponse.toBankAccountResponse(): BankAccountResponse =
    BankAccountResponse(
        id = id,
        userId = userId,
        balance = balance,
        currency = currency,
        accountName = accountName,
        bankName = bankName,
        status = status,
        accountTypes = accountTypes,
        accountDetails = accountDetails,
        plaid = plaid,
        bankAddress = bankAddress,
        billingDetails = billingDetails,
        circle = circle,
        timestamp = timestamp
    )

data class CreateBankAccountError(
    val code: Int,
    val message: String?,
    val details: List<CreateBankAccountValidationError>
)

data class CreateBankAccountValidationError(
    val error: String?,
    val location: String?,
    val invalidValue: String?,
    val message: String?
)