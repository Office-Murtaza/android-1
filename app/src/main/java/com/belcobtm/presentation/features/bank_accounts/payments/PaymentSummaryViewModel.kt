package com.belcobtm.presentation.features.bank_accounts.payments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.bank_account.interactor.CreateBankAccountPaymentUseCase
import com.belcobtm.domain.bank_account.item.BankAccountCreatePaymentDataItem
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem
import com.belcobtm.domain.bank_account.item.PaymentInstructionsDataItem
import com.belcobtm.domain.bank_account.type.BankAccountPaymentType
import com.belcobtm.domain.bank_account.type.BankAccountType
import com.belcobtm.domain.transaction.interactor.CreateTransactionToAddressUseCase
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.extensions.code
import com.belcobtm.presentation.core.mvvm.LoadingData
import wallet.core.jni.CoinType

class PaymentSummaryViewModel(
    private val createBankAccountPaymentUseCase: CreateBankAccountPaymentUseCase,
    private val createTransactionToAddressUseCase: CreateTransactionToAddressUseCase,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
) : ViewModel() {
    private lateinit var _usdcDataItem: CoinDataItem
    private lateinit var _usdcTransactionPlanItem: TransactionPlanItem

    private val _bankAccountPaymentLiveData =
        MutableLiveData<LoadingData<BankAccountPaymentListItem>>()
    val bankAccountPaymentLiveData: LiveData<LoadingData<BankAccountPaymentListItem>> =
        _bankAccountPaymentLiveData

    fun createPayment(
        walletId: String?,
        accountType: BankAccountType?,
        accountId: String?,
        bankAccountId: String,
        transferAmount: Double,
        paymentAmount: Double,
        networkFee: Double,
        paymentInstructions: PaymentInstructionsDataItem?
    ) {
        _bankAccountPaymentLiveData.value = LoadingData.Loading()
        createBankAccountPaymentUseCase.invoke(CreateBankAccountPaymentUseCase.Params(
            BankAccountCreatePaymentDataItem(
                type = BankAccountPaymentType.BUY,
                bankAccountId = bankAccountId,
                transferHex = null,
                transferSourceId = walletId,
                transferSourceAddress = null,
                transferDestinationAddress = _usdcDataItem.details.walletAddress,
                transferAmount = transferAmount,
                paymentDestinationType = null,
                paymentDestinationId = null,
                paymentAmount = paymentAmount,
                paymentSourceId = accountId,
                paymentSourceType = accountType,
                paymentBeneficiaryEmail = null,
                paymentEmail = "abc@gmail.com",
                cryptoFee = networkFee,
                cryptoFeeCurrency = "USDC",
                paymentInstructions = paymentInstructions
            )
        ),
            onSuccess = { response ->
                _bankAccountPaymentLiveData.value = LoadingData.Success(response)
            }, onError = {
                _bankAccountPaymentLiveData.value = LoadingData.Error(it)
            })
    }

    fun createPayout(
        walletId: String?,
        accountType: BankAccountType?,
        accountId: String?,
        bankAccountId: String,
        transferAmount: Double,
        paymentAmount: Double,
        walletAddress: String,
        networkFee: Double,
    ) {
        _bankAccountPaymentLiveData.value = LoadingData.Loading()
        createTransactionToAddressUseCase.invoke(CreateTransactionToAddressUseCase.Params(
            useMaxAmountFlag = false,
            fromCoin = _usdcDataItem.code,
            fromCoinAmount = transferAmount,
            transactionPlanItem = _usdcTransactionPlanItem,
            toAddress = walletAddress,
        ), onSuccess = { hex ->
            createBankAccountPaymentUseCase.invoke(CreateBankAccountPaymentUseCase.Params(
                BankAccountCreatePaymentDataItem(
                    type = BankAccountPaymentType.SELL,
                    bankAccountId = bankAccountId,
                    transferHex = hex,
                    transferSourceId = walletId,
                    transferSourceAddress = _usdcDataItem.details.walletAddress,
                    transferDestinationAddress = null,
                    transferAmount = transferAmount,
                    paymentDestinationType = accountType,
                    paymentDestinationId = accountId,
                    paymentAmount = paymentAmount,
                    paymentSourceId = null,
                    paymentSourceType = null,
                    paymentBeneficiaryEmail = "abc@gmail.com",
                    paymentEmail = null,
                    cryptoFee = networkFee,
                    cryptoFeeCurrency = CoinType.ETHEREUM.code(),
                    paymentInstructions = null,
                )
            ),
                onSuccess = { response ->
                    _bankAccountPaymentLiveData.value = LoadingData.Success(response)
                }, onError = {
                    _bankAccountPaymentLiveData.value = LoadingData.Error(it)
                })
        }, onError = {
            _bankAccountPaymentLiveData.value = LoadingData.Error(it)
        })

    }

    fun getUsdcInfo() {
        getCoinByCodeUseCase.invoke(LocalCoinType.USDC.name, onSuccess = { usdc ->
            _usdcDataItem = usdc
        }, onError = {
        })
        getTransactionPlanUseCase.invoke(LocalCoinType.USDC.name, onSuccess = { usdcPlan ->
            _usdcTransactionPlanItem = usdcPlan
        }, onError = {
        })
    }
}