package com.belcobtm.data.rest.bank_account.response

import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem
import com.belcobtm.domain.bank_account.type.BankAccountPaymentStatusType
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.presentation.core.DateFormat

data class BankAccountPaymentsResponse(
    val walletId: String,
    val feePercent: String,
    val walletAddress: String,
    val payments: List<BankAccountPayment>,
    val txLimit: BankAccountLimit
)

data class BankAccountPayment(
    val id: String,
    val userId: String?,
    val bankAccountId: String,
    val accountType: String,
    val type: String,
    val transfer: PaymentTransfer,
    val payment: Payment,
    val step: Int,
    val cryptoFee: Double,
    val cryptoFeeCurrency: String,
    val finish: Boolean,
    val timestamp: Long
)

fun BankAccountPayment.mapToDataItem(): BankAccountPaymentListItem =
    BankAccountPaymentListItem(
        id = id,
        step = step,
        bankAccountId = bankAccountId,
        platformFee = 0.0,
        paymentType = BankAccountPaymentType.fromString(type),
        accountType = BankAccountType.fromString(accountType),
        usdPaymentStatus = BankAccountPaymentStatusType.fromString(payment.response?.data?.status),
        usdcTransferStatus = BankAccountPaymentStatusType.fromString(transfer.status),
        usdcTransferHash = transfer.hash,
        usdAmount = payment.request?.amount?.amount ?: "",
        usdcAmount = transfer.request?.amount?.amount ?: "",
        date = DateFormat.sdfLong.format(timestamp),
        timestamp = timestamp,
        networkFee = cryptoFee,
        networkFeeCurrency = cryptoFeeCurrency,

        )

data class BankAccountLimit(
    val ACH: String?,
    val WIRE: String?,
)

data class PaymentTransfer(
    val hex: String?,
    val hash: String?,
    val status: String?,
    val request: PaymentTransferRequest?,
    val response: PaymentTransferResponse?,
)

data class PaymentTransferResponse(
    val id: String?,
)

data class PaymentTransferRequest(
    val metadata: PaymentTransferRequestMetadata?,
    val amount: PaymentTransferRequestAmount?,
    val source: PaymentTransferRequestSource?,
    val destination: PaymentTransferRequestDestination?,
    val idempotencyKey: String?
)

data class PaymentTransferRequestMetadata(
    val email: String?,
    val phoneNumber: String?,
    val beneficiaryEmail: String?,
    val sessionId: String?,
    val ipAddress: String?,
)

data class PaymentTransferRequestAmount(
    val currency: String?,
    val amount: String?,
)

data class PaymentTransferRequestSource(
    val id: String?,
    val type: String?,
    val address: String?,
    val chain: String?
)

data class PaymentTransferRequestDestination(
    val id: String?,
    val type: String?,
    val address: String?,
    val chain: String?,
)


data class Payment(
    val request: PaymentRequest?,
    val response: PaymentResponse?,
    val instructions: PaymentInstructions?,
)

data class PaymentInstructions(
    val trackingRef: String?,
    val beneficiary: InstructionsBeneficiary?,
    val virtualAccountEnabled: Boolean?,
    val beneficiaryBank: InstructionsBeneficiaryBank?,
)

data class InstructionsBeneficiary(
    val name: String?,
    val address1: String?,
    val address2: String?,
)

data class InstructionsBeneficiaryBank(
    val name: String?,
    val address: String?,
    val city: String?,
    val postalCode: String?,
    val country: String?,
    val swiftCode: String?,
    val routingNumber: String?,
    val accountNumber: String?,
)

data class PaymentRequest(
    val metadata: PaymentRequestMetadata?,
    val amount: PaymentRequestAmount?,
    val source: PaymentRequestSource?,
    val destination: PaymentRequestDestination?,
    val autoCapture: Boolean?,
    val verification: String?,
    val idempotencyKey: String?,
    val trackingRef: String?,
)

data class PaymentRequestMetadata(
    val email: String?,
    val phoneNumber: String?,
    val beneficiaryEmail: String?,
)

data class PaymentRequestAmount(
    val currency: String?,
    val amount: String?
)

data class PaymentRequestSource(
    val id: String?,
    val type: String?,
    val address: String?,
    val chain: String?
)

data class PaymentRequestDestination(
    val id: String?,
    val type: String?,
    val address: String?,
    val chain: String?,
)

data class PaymentResponse(
    val data: PaymentResponseData?,
)

data class PaymentResponseData(
    val id: String?,
    val type: String?,
    val status: String?,
    val merchantId: String?,
    val merchantWalletId: String?,
    val description: String?,
    val amount: PaymentResponseAmount,
    val metadata: PaymentResponseMetadata
)

data class PaymentResponseMetadata(
    val email: String?,
    val phoneNumber: String?,
    val beneficiaryEmail: String?,
    val sessionId: String?,
    val ipAddress: String?,
)

data class PaymentResponseAmount(
    val currency: String,
    val amount: String,
)