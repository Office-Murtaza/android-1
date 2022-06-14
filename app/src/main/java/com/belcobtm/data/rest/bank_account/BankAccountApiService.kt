package com.belcobtm.data.rest.bank_account

import com.belcobtm.data.rest.bank_account.request.CreateBankAccountPaymentRequest
import com.belcobtm.data.rest.bank_account.request.CreateBankAccountRequest
import com.belcobtm.data.rest.bank_account.request.LinkBankAccountRequest
import com.belcobtm.data.rest.bank_account.response.AccountDetails
import com.belcobtm.data.rest.bank_account.response.BankAccountPayment
import com.belcobtm.data.rest.bank_account.response.BankAccountPaymentsResponse
import com.belcobtm.data.rest.bank_account.response.BankAccountResponse
import com.belcobtm.data.rest.bank_account.response.BankAddress
import com.belcobtm.data.rest.bank_account.response.BillingDetails
import com.belcobtm.data.rest.bank_account.response.CreateBankAccountResponse
import com.belcobtm.data.rest.bank_account.response.InstructionsBeneficiary
import com.belcobtm.data.rest.bank_account.response.InstructionsBeneficiaryBank
import com.belcobtm.data.rest.bank_account.response.LinkTokenResponse
import com.belcobtm.data.rest.bank_account.response.Payment
import com.belcobtm.data.rest.bank_account.response.PaymentInstructions
import com.belcobtm.data.rest.bank_account.response.PaymentRequest
import com.belcobtm.data.rest.bank_account.response.PaymentRequestAmount
import com.belcobtm.data.rest.bank_account.response.PaymentRequestDestination
import com.belcobtm.data.rest.bank_account.response.PaymentRequestMetadata
import com.belcobtm.data.rest.bank_account.response.PaymentRequestSource
import com.belcobtm.data.rest.bank_account.response.PaymentTransfer
import com.belcobtm.data.rest.bank_account.response.PaymentTransferRequest
import com.belcobtm.data.rest.bank_account.response.PaymentTransferRequestAmount
import com.belcobtm.data.rest.bank_account.response.PaymentTransferRequestDestination
import com.belcobtm.data.rest.bank_account.response.PaymentTransferRequestSource
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.bank_account.item.BankAccountCreateDataItem
import com.belcobtm.domain.bank_account.item.BankAccountCreatePaymentDataItem
import com.belcobtm.domain.bank_account.item.BankAccountLinkDataItem
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.extensions.toStringPercents
import java.util.UUID

class BankAccountApiService(private val api: BankAccountApi) {

    // test userId "624f09650d76a408d7215e90"
    suspend fun getBankAccountsList(userId: String): Either<Failure, List<BankAccountResponse>> =
        try {
            val request = api.getBankAccountsListAsync(userId)
            request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

    suspend fun getBankAccountPayments(
        userId: String,
        accountId: String
    ): Either<Failure, BankAccountPaymentsResponse> =
        try {
            val request = api.getBankAccountPaymentsAsync(userId, accountId)
            request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

    suspend fun createBankAccount(
        userId: String,
        bankAccountDataItem: BankAccountCreateDataItem
    ): Either<Failure, CreateBankAccountResponse> = try {
        val request = with(bankAccountDataItem) {
            CreateBankAccountRequest(
                bankAddress = BankAddress(
                    country = bankCountry,
                    city = bankCity
                ),
                bankName = bankName,
                billingDetails = BillingDetails(
                    name = name,
                    email = email,
                    phone = phone,
                    country = country,
                    region = province,
                    city = city,
                    street = address,
                    postalCode = zipCode
                ),
                accountDetails = AccountDetails(
                    accountNumber = accountNumber,
                    routingNumber = routingNumber,
                    iban = iban
                )
            )
        }
        val response = api.createBankAccountAsync(userId, request)
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun linkBankAccounts(
        userId: String,
        linkedBankAccountsDataItem: BankAccountLinkDataItem
    ): Either<Failure, List<BankAccountResponse>> = try {
        val request = with(linkedBankAccountsDataItem) {
            LinkBankAccountRequest(
                publicToken = publicToken,
                accountIds = accountsId,
                bankName = bankName
            )
        }
        val response = api.linkBankAccountAsync(userId, request)
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getLinkToken(
        userId: String,
    ): Either<Failure, LinkTokenResponse> = try {
        val response = api.getLinkTokenAsync(userId, "ANDROID")
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun createBankAccountPayment(
        userId: String,
        bankAccountPaymentDataItem: BankAccountCreatePaymentDataItem,
        lat: Double?,
        lng: Double?,
    ): Either<Failure, BankAccountPayment> = try {
        val request = with(bankAccountPaymentDataItem) {
            when (type) {
                BankAccountPaymentType.BUY -> {
                    if (paymentSourceType == BankAccountType.ACH)
                    // BUY ACH
                        CreateBankAccountPaymentRequest(
                            type = type.stringValue,
                            bankAccountId = bankAccountId,
                            accountType = BankAccountType.ACH.stringValue,
                            cryptoFee = cryptoFee,
                            cryptoFeeCurrency = cryptoFeeCurrency,
                            transfer = PaymentTransfer(
                                hex = null,
                                hash = null,
                                status = null,
                                request = PaymentTransferRequest(
                                    metadata = null,
                                    source = PaymentTransferRequestSource(
                                        transferSourceId,
                                        "wallet",
                                        null,
                                        null
                                    ),
                                    destination = PaymentTransferRequestDestination(
                                        type = "blockchain",
                                        address = transferDestinationAddress,
                                        chain = "ETH",
                                        id = null,
                                    ),
                                    amount = PaymentTransferRequestAmount(
                                        "USD",
                                        transferAmount.toStringCoin()
                                    ),
                                    idempotencyKey = UUID.randomUUID().toString()
                                ),
                                response = null,
                            ),
                            payment = Payment(
                                request = PaymentRequest(
                                    metadata = PaymentRequestMetadata(
                                        email = paymentEmail,
                                        phoneNumber = null,
                                        beneficiaryEmail = null,
                                    ),
                                    amount = PaymentRequestAmount(
                                        amount = paymentAmount.toStringPercents(),
                                        currency = "USD"
                                    ),
                                    source = PaymentRequestSource(
                                        id = paymentSourceId,
                                        type = paymentSourceType.stringValue.lowercase(),
                                        address = null,
                                        chain = null,
                                    ),
                                    destination = null,
                                    autoCapture = true,
                                    verification = "none",
                                    idempotencyKey = UUID.randomUUID().toString(),
                                    trackingRef = null
                                ),
                                response = null,
                                instructions = null
                            ),
                            latitude = lat,
                            longitude = lng
//                        latitude = 40.74371337890625,
//                        longitude = -73.980727954218736,
                        ) else
                    //BUY WIRE
                        CreateBankAccountPaymentRequest(
                            type = type.stringValue,
                            bankAccountId = bankAccountId,
                            accountType = BankAccountType.WIRE.stringValue,
                            cryptoFee = cryptoFee,
                            cryptoFeeCurrency = cryptoFeeCurrency,
                            transfer = PaymentTransfer(
                                hex = null,
                                hash = null,
                                status = null,
                                request = PaymentTransferRequest(
                                    metadata = null,
                                    source = PaymentTransferRequestSource(
                                        transferSourceId,
                                        "wallet",
                                        null,
                                        null
                                    ),
                                    destination = PaymentTransferRequestDestination(
                                        type = "blockchain",
                                        address = transferDestinationAddress,
                                        chain = "ETH",
                                        id = null,
                                    ),
                                    amount = PaymentTransferRequestAmount(
                                        "USD",
                                        transferAmount.toStringCoin()
                                    ),
                                    idempotencyKey = UUID.randomUUID().toString()
                                ),
                                response = null,
                            ),
                            payment = Payment(
                                request = PaymentRequest(
                                    metadata = null,
                                    amount = PaymentRequestAmount(
                                        amount = paymentAmount.toStringPercents(),
                                        currency = "USD"
                                    ),
                                    source = null,
                                    destination = null,
                                    autoCapture = null,
                                    verification = "none",
                                    idempotencyKey = UUID.randomUUID().toString(),
                                    trackingRef = paymentInstructions?.trackingRef
                                ),
                                instructions = PaymentInstructions(
                                    trackingRef = paymentInstructions?.trackingRef,
                                    beneficiary = InstructionsBeneficiary(
                                        name = paymentInstructions?.beneficiaryName,
                                        address1 = paymentInstructions?.beneficiaryAddress1,
                                        address2 = paymentInstructions?.beneficiaryAddress2,
                                    ),
                                    beneficiaryBank = InstructionsBeneficiaryBank(
                                        name = paymentInstructions?.beneficiaryBankName,
                                        address = paymentInstructions?.beneficiaryBankAddress,
                                        city = paymentInstructions?.beneficiaryBankCity,
                                        postalCode = paymentInstructions?.beneficiaryBankPostalCode,
                                        country = paymentInstructions?.beneficiaryBankCountry,
                                        swiftCode = paymentInstructions?.beneficiaryBankSwiftCode,
                                        routingNumber = paymentInstructions?.beneficiaryBankRoutingNumber,
                                        accountNumber = paymentInstructions?.beneficiaryAccountNumber
                                    ),
                                    virtualAccountEnabled = paymentInstructions?.virtualAccountEnabled
                                ),
                                response = null
                            ),
                            latitude = lat,
                            longitude = lng
                        )
//                        latitude = 40.74371337890625,
//                        longitude = -73.980727954218736,
                }

                BankAccountPaymentType.SELL ->
                    //SELL ACH and WIRE
                    CreateBankAccountPaymentRequest(
                        type = type.stringValue,
                        accountType = paymentDestinationType?.stringValue,
                        bankAccountId = bankAccountId,
                        cryptoFee = cryptoFee,
                        cryptoFeeCurrency = cryptoFeeCurrency,
                        transfer = PaymentTransfer(
                            hex = transferHex,
                            hash = null,
                            status = null,
                            request = PaymentTransferRequest(
                                metadata = null,
                                source = PaymentTransferRequestSource(
                                    null,
                                    "blockchain",
                                    transferSourceAddress,
                                    "ETH"
                                ),
                                destination = null,
                                amount = PaymentTransferRequestAmount(
                                    "USD",
                                    transferAmount.toStringCoin()
                                ),
                                idempotencyKey = UUID.randomUUID().toString()
                            ),
                            response = null,
                        ),
                        payment = Payment(
                            request = PaymentRequest(
                                metadata = PaymentRequestMetadata(
                                    email = null,
                                    phoneNumber = null,
                                    beneficiaryEmail = paymentBeneficiaryEmail,
                                ),
                                amount = PaymentRequestAmount(
                                    amount = paymentAmount.toStringPercents(),
                                    currency = "USD"
                                ),
                                source = null,
                                destination = PaymentRequestDestination(
                                    id = paymentDestinationId,
                                    type = paymentDestinationType?.stringValue,
                                    address = null,
                                    chain = null,
                                ),
                                autoCapture = null,
                                verification = null,
                                idempotencyKey = UUID.randomUUID().toString(),
                                trackingRef = null,
                            ),
                            response = null,
                            instructions = null
                        ),
                        latitude = lat,
                        longitude = lng
//                        latitude = 40.74371337890625,
//                        longitude = -73.980727954218736,
                    )
                BankAccountPaymentType.NONE -> {
                    TODO()
                }
            }
        }
        val response = api.createBankAccountPaymentAsync(userId, request)
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

}
