package com.belcobtm.data.rest.bank_account

import com.belcobtm.data.rest.bank_account.request.CreateBankAccountPaymentRequest
import com.belcobtm.data.rest.bank_account.request.CreateBankAccountRequest
import com.belcobtm.data.rest.bank_account.request.LinkBankAccountRequest
import com.belcobtm.data.rest.bank_account.response.*
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface BankAccountApi {
    @GET("payment/user/{userId}/bankAccounts")
    fun getBankAccountsListAsync(
        @Path("userId") userId: String
    ): Deferred<Response<List<BankAccountResponse>>>

    @POST("payment/user/{userId}/bankAccount")
    fun createBankAccountAsync(
        @Path("userId") userId: String,
        @Body request: CreateBankAccountRequest
    ): Deferred<Response<CreateBankAccountResponse>>

    @POST("payment/plaid/user/{userId}/link/accounts")
    fun linkBankAccountAsync(
        @Path("userId") userId: String,
        @Body request: LinkBankAccountRequest
    ): Deferred<Response<List<BankAccountResponse>>>

    @POST("payment/plaid/user/{userId}/link/token")
    fun getLinkTokenAsync(
        @Path("userId") userId: String,
        @Query("platform") platform: String,
    ): Deferred<Response<LinkTokenResponse>>

    @GET("payment/user/{userId}/payment")
    fun getBankAccountPaymentsAsync(
        @Path("userId") userId: String,
        @Query("bankAccountId") accountId: String,
    ): Deferred<Response<BankAccountPaymentsResponse>>

    @POST("payment/user/{userId}/payment")
    fun createBankAccountPaymentAsync(
        @Path("userId") userId: String,
        @Body request: CreateBankAccountPaymentRequest
    ): Deferred<Response<BankAccountPayment>>
}