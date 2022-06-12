package com.belcobtm.domain.bank_account.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentInstructionsDataItem(
    val trackingRef: String?,
    val beneficiaryName: String?,
    val beneficiaryAddress1: String?,
    val beneficiaryAddress2: String?,
    val virtualAccountEnabled: Boolean?,
    val beneficiaryBankName: String?,
    val beneficiaryBankAddress: String?,
    val beneficiaryBankCity: String?,
    val beneficiaryBankPostalCode: String?,
    val beneficiaryBankCountry: String?,
    val beneficiaryBankSwiftCode: String?,
    val beneficiaryBankRoutingNumber: String?,
    val beneficiaryAccountNumber: String?,
) : Parcelable

