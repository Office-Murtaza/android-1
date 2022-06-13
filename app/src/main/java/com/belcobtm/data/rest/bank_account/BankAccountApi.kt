package com.belcobtm.data.rest.bank_account

import com.belcobtm.data.rest.bank_account.request.CreateBankAccountPaymentRequest
import com.belcobtm.data.rest.bank_account.request.CreateBankAccountRequest
import com.belcobtm.data.rest.bank_account.request.LinkBankAccountRequest
import com.belcobtm.data.rest.bank_account.response.BankAccountPayment
import com.belcobtm.data.rest.bank_account.response.BankAccountPaymentsResponse
import com.belcobtm.data.rest.bank_account.response.BankAccountResponse
import com.belcobtm.data.rest.bank_account.response.CreateBankAccountResponse
import com.belcobtm.data.rest.bank_account.response.LinkTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BankAccountApi {

    @GET("payment/user/{userId}/bankAccounts")
    suspend fun getBankAccountsListAsync(
        @Path("userId") userId: String
    ): Response<List<BankAccountResponse>>

    @POST("payment/user/{userId}/bankAccount")
    suspend fun createBankAccountAsync(
        @Path("userId") userId: String,
        @Body request: CreateBankAccountRequest
    ): Response<CreateBankAccountResponse>

    @POST("payment/plaid/user/{userId}/link/accounts")
    suspend fun linkBankAccountAsync(
        @Path("userId") userId: String,
        @Body request: LinkBankAccountRequest
    ): Response<List<BankAccountResponse>>

    @POST("payment/plaid/user/{userId}/link/token")
    suspend fun getLinkTokenAsync(
        @Path("userId") userId: String,
        @Query("platform") platform: String,
    ): Response<LinkTokenResponse>

    @GET("payment/user/{userId}/payment")
    suspend fun getBankAccountPaymentsAsync(
        @Path("userId") userId: String,
        @Query("bankAccountId") accountId: String,
    ): Response<BankAccountPaymentsResponse>

    @POST("payment/user/{userId}/payment")
    suspend fun createBankAccountPaymentAsync(
        @Path("userId") userId: String,
        @Body request: CreateBankAccountPaymentRequest
    ): Response<BankAccountPayment>

}
