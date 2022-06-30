package com.belcobtm.domain.bank_account

import com.belcobtm.data.model.bank_account.BankAccountsData
import com.belcobtm.data.model.payments.PaymentsData
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.bank_account.item.BankAccountCreateDataItem
import com.belcobtm.domain.bank_account.item.BankAccountCreatePaymentDataItem
import com.belcobtm.domain.bank_account.item.BankAccountCreateResponseDataItem
import com.belcobtm.domain.bank_account.item.BankAccountDataItem
import com.belcobtm.domain.bank_account.item.BankAccountLinkDataItem
import com.belcobtm.domain.bank_account.item.BankAccountPaymentDataItem
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem
import kotlinx.coroutines.flow.Flow

interface BankAccountRepository {

    fun observeBankAccounts(): Flow<BankAccountsData>
    fun observePayments(): Flow<PaymentsData>
    suspend fun getBankAccountsList(): Either<Failure, List<BankAccountDataItem>>
    suspend fun getBankAccountPayments(accountId: String): Either<Failure, BankAccountPaymentDataItem>
    suspend fun createBankAccount(bankAccountCreateDataItem: BankAccountCreateDataItem): Either<Failure, BankAccountCreateResponseDataItem>
    suspend fun createBankAccountPayment(bankAccountPaymentDataItem: BankAccountCreatePaymentDataItem): Either<Failure, BankAccountPaymentListItem>
    suspend fun linkBankAccount(bankAccountLinkDataItem: BankAccountLinkDataItem): Either<Failure, List<BankAccountDataItem>>
    suspend fun getLinkToken(): Either<Failure, String>
}
