package com.belcobtm.data

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.inmemory.bank_accounts.BankAccountsInMemoryCache
import com.belcobtm.data.inmemory.payments.PaymentsInMemoryCache
import com.belcobtm.data.model.bank_account.BankAccountsData
import com.belcobtm.data.model.payments.PaymentsData
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.data.rest.bank_account.BankAccountApiService
import com.belcobtm.data.rest.bank_account.response.CreateBankAccountValidationError
import com.belcobtm.data.rest.bank_account.response.mapToDataItem
import com.belcobtm.data.rest.bank_account.response.toBankAccountResponse
import com.belcobtm.data.rest.bank_account.response.toDataItem
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.bank_account.BankAccountRepository
import com.belcobtm.domain.bank_account.item.BankAccountCreateDataItem
import com.belcobtm.domain.bank_account.item.BankAccountCreatePaymentDataItem
import com.belcobtm.domain.bank_account.item.BankAccountCreateResponseDataItem
import com.belcobtm.domain.bank_account.item.BankAccountDataItem
import com.belcobtm.domain.bank_account.item.BankAccountLimitDataItem
import com.belcobtm.domain.bank_account.item.BankAccountLinkDataItem
import com.belcobtm.domain.bank_account.item.BankAccountPaymentDataItem
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem
import com.belcobtm.domain.bank_account.item.BankAccountValidationErrorDataItem
import kotlinx.coroutines.flow.Flow

class BankAccountRepositoryImpl(
    private val apiService: BankAccountApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val bankAccountCache: BankAccountsInMemoryCache,
    private val paymentsCache: PaymentsInMemoryCache,
    private val locationProvide: LocationProvider
) : BankAccountRepository {

    override fun observeBankAccounts(): Flow<BankAccountsData> = bankAccountCache.observableData

    override fun observePayments(): Flow<PaymentsData> = paymentsCache.observableData

    override suspend fun getBankAccountsList(): Either<Failure, List<BankAccountDataItem>> {
        val response = apiService.getBankAccountsList(prefHelper.userId)
        return if (response.isRight) {

            val responseItem = (response as Either.Right).b

            bankAccountCache.init(responseItem)

            val listOfBankAccounts = mutableListOf<BankAccountDataItem>()
            for (bankAccountModel in responseItem) {
                listOfBankAccounts.add(
                    bankAccountModel.mapToDataItem()
                )
            }
            Either.Right(
                listOfBankAccounts
            )
        } else {
            response as Either.Left
        }
    }

    override suspend fun getBankAccountPayments(accountId: String): Either<Failure, BankAccountPaymentDataItem> {
        val response = apiService.getBankAccountPayments(prefHelper.userId, accountId)
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b

            paymentsCache.init(responseItem.payments)

            val listOfPayments = mutableListOf<BankAccountPaymentListItem>()
            for (paymentModel in responseItem.payments) {
                listOfPayments.add(
                    paymentModel.mapToDataItem()
                )
            }
            Either.Right(
                BankAccountPaymentDataItem(
                    walletId = responseItem.walletId,
                    walletAddress = responseItem.walletAddress,
                    feePercent = responseItem.feePercent.toInt(),
                    payments = listOfPayments,
                    limit = BankAccountLimitDataItem(
                        achLimit = responseItem.txLimit.ACH?.toInt(),
                        wireLimit = responseItem.txLimit.WIRE?.toInt()
                    )
                )
            )
        } else {
            response as Either.Left
        }
    }

    override suspend fun createBankAccount(bankAccountCreateDataItem: BankAccountCreateDataItem): Either<Failure, BankAccountCreateResponseDataItem> {
        val response = apiService.createBankAccount(
            prefHelper.userId, bankAccountCreateDataItem
        )
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            return if (responseItem.error != null) {
                val createBankAccountValidationError = BankAccountValidationErrorDataItem()
                for (validationError in responseItem.error.details) {
                    when (validationError.location) {
                        ACCOUNT_NUMBER -> {
                            createBankAccountValidationError.accountNumberValidationError =
                                createValidationErrorString(validationError)
                        }
                        ROUTING_NUMBER -> {
                            createBankAccountValidationError.routingNumberValidationError =
                                createValidationErrorString(validationError)
                        }
                        IBAN -> {
                            createBankAccountValidationError.ibanValidationError =
                                createValidationErrorString(validationError)
                        }
                        BANK_NAME -> {
                            createBankAccountValidationError.bankNameValidationError =
                                createValidationErrorString(validationError)
                        }
                        BANK_CITY -> {
                            createBankAccountValidationError.bankCityValidationError =
                                createValidationErrorString(validationError)
                        }
                        BANK_COUNTRY -> {
                            createBankAccountValidationError.bankCountryValidationError =
                                createValidationErrorString(validationError)
                        }
                        NAME -> {
                            createBankAccountValidationError.nameValidationError =
                                createValidationErrorString(validationError)
                        }
                        CITY -> {
                            createBankAccountValidationError.cityValidationError =
                                createValidationErrorString(validationError)
                        }
                        COUNTRY -> {
                            createBankAccountValidationError.countryValidationError =
                                createValidationErrorString(validationError)
                        }
                        ADDRESS -> {
                            createBankAccountValidationError.addressValidationError =
                                createValidationErrorString(validationError)
                        }
                        PROVINCE -> {
                            createBankAccountValidationError.provinceValidationError =
                                createValidationErrorString(validationError)
                        }
                        ZIP_CODE -> {
                            createBankAccountValidationError.zipCodeValidationError =
                                createValidationErrorString(validationError)
                        }
                    }
                }
                Either.Right(
                    BankAccountCreateResponseDataItem(
                        validationError =
                        createBankAccountValidationError
                    )
                )
            } else {
                val bankAccount = responseItem.toDataItem()
                bankAccountCache.init(listOf(responseItem.toBankAccountResponse()))
                Either.Right(bankAccount)
            }
        } else {
            response as Either.Left
        }
    }

    override suspend fun linkBankAccount(bankAccountLinkDataItem: BankAccountLinkDataItem): Either<Failure, List<BankAccountDataItem>> {
        val response = apiService.linkBankAccounts(
            prefHelper.userId, bankAccountLinkDataItem
        )
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            val listOfBankAccounts = mutableListOf<BankAccountDataItem>()
            for (bankAccountModel in responseItem) {
                listOfBankAccounts.add(
                    bankAccountModel.mapToDataItem()
                )
            }
            bankAccountCache.init(responseItem)
            Either.Right(
                listOfBankAccounts
            )
        } else {
            response as Either.Left
        }

    }

    override suspend fun createBankAccountPayment(bankAccountPaymentDataItem: BankAccountCreatePaymentDataItem): Either<Failure, BankAccountPaymentListItem> {
        val response = apiService.createBankAccountPayment(
            prefHelper.userId, bankAccountPaymentDataItem,
            locationProvide.getCurrentLocation()?.latitude,
            locationProvide.getCurrentLocation()?.longitude
        )
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            val bankAccountPayment: BankAccountPaymentListItem = responseItem.mapToDataItem()

            paymentsCache.init(listOf(responseItem))
            Either.Right(
                bankAccountPayment
            )
        } else {
            response as Either.Left
        }

    }

    override suspend fun getLinkToken(): Either<Failure, String> {
        val response = apiService.getLinkToken(
            prefHelper.userId
        )
        return if (response.isRight) {
            val responseItem = (response as Either.Right).b
            return Either.Right(
                responseItem.linkToken!!
            )
        } else {
            response as Either.Left
        }
    }

    companion object {

        const val ACCOUNT_NUMBER = "accountNumber"
        const val ROUTING_NUMBER = "routingNumber"
        const val IBAN = "iban"
        const val BANK_NAME = "bankAddress.bankName"
        const val BANK_CITY = "bankAddress.city"
        const val BANK_COUNTRY = "bankAddress.country"
        const val NAME = "billingDetails.name"
        const val CITY = "billingDetails.city"
        const val COUNTRY = "billingDetails.country"
        const val ADDRESS = "billingDetails.line1"
        const val PROVINCE = "billingDetails.district"
        const val ZIP_CODE = "billingDetails.postalCode"

        fun createValidationErrorString(validationError: CreateBankAccountValidationError): String {
            return validationError.message?.replaceFirst(
                validationError.location ?: "",
                validationError.invalidValue ?: ""
            ) ?: ""
        }
    }

}
