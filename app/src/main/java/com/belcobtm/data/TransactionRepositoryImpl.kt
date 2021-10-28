package com.belcobtm.data

import com.belcobtm.data.core.TransactionHelper
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.toDataItem
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.inmemory.transactions.TransactionsInMemoryCache
import com.belcobtm.data.model.transactions.TransactionsData
import com.belcobtm.data.rest.transaction.TransactionApiService
import com.belcobtm.data.rest.transaction.response.hash.UtxoItemResponse
import com.belcobtm.domain.*
import com.belcobtm.domain.tools.ToolsRepository
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.*
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isBtcCoin
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.extensions.customPurpose
import com.belcobtm.presentation.core.extensions.customXpubVersion
import kotlinx.coroutines.flow.Flow
import wallet.core.jni.HDWallet

class TransactionRepositoryImpl(
    private val apiService: TransactionApiService,
    private val walletDao: WalletDao,
    private val toolsRepository: ToolsRepository,
    private val cache: TransactionsInMemoryCache,
    private val transactionRepository: TransactionHelper,
    private val daoAccount: AccountDao,
    private val preferencesHelper: SharedPreferencesHelper
) : TransactionRepository {

    private var utxosPerCoint: MutableMap<String, List<UtxoItemResponse>> = HashMap()

    override suspend fun getTransactionPlan(coinCode: String): Either<Failure, TransactionPlanItem> =
        if (coinCode.isBtcCoin()) {
            val coin = LocalCoinType.valueOf(coinCode).trustWalletType
            val hdWallet = HDWallet(preferencesHelper.apiSeed, "")
            val publicKey = hdWallet.getExtendedPublicKey(
                coin.customPurpose(), coin, coin.customXpubVersion()
            )
            apiService.getUtxoList(coinCode, publicKey).flatMapSuspend {
                utxosPerCoint[coinCode] = it
                apiService.getTransactionPlan(coinCode)
            }
        } else {
            utxosPerCoint.remove(coinCode)
            apiService.getTransactionPlan(coinCode)
        }

    override suspend fun fetchTransactionList(coinCode: String): Either<Failure, Unit> =
        apiService.fetchTransactions(coinCode).map {
            cache.init(coinCode, it.transactions)
        }

    override fun observeTransactions(): Flow<TransactionsData> =
        cache.observableData

    override suspend fun getSignedPlan(
        fromCoin: String,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        toAddress: String,
        useMaxAmountFlag: Boolean
    ): Either<Failure, SignedTransactionPlanItem> = transactionRepository.getSignedTransactionPlan(
        toAddress,
        LocalCoinType.valueOf(fromCoin),
        fromCoinAmount,
        fromTransactionPlan,
        useMaxAmountFlag,
        utxosPerCoint[fromCoin].orEmpty()
    )

    override suspend fun createTransaction(
        useMaxAmountFlag: Boolean,
        fromCoin: String,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        isNeedSendSms: Boolean
    ): Either<Failure, String> {
        val toAddress = getCoinByCode(fromCoin).details.walletAddress
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse =
            transactionRepository.createTransactionHash(
                useMaxAmountFlag, toAddress, coinType, fromCoinAmount,
                fromTransactionPlan, utxosPerCoint[fromCoin].orEmpty()
            )
        return when {
            isNeedSendSms && hashResponse.isRight -> {
                val sendSmsToDeviceResponse = toolsRepository.sendSmsToDeviceOld()
                if (sendSmsToDeviceResponse.isRight) {
                    hashResponse as Either.Right
                } else {
                    sendSmsToDeviceResponse as Either.Left
                }
            }
            !isNeedSendSms && hashResponse.isRight -> hashResponse as Either.Right
            else -> hashResponse as Either.Left
        }
    }

    override suspend fun receiverAccountActivated(
        fromCoin: String,
        toAddress: String
    ): Either<Failure, Boolean> =
        apiService.receiverAccountActivated(fromCoin, toAddress).map { it.result }

    override suspend fun withdraw(
        useMaxAmountFlag: Boolean,
        toAddress: String,
        fromCoin: String,
        fromCoinAmount: Double,
        fee: Double,
        fromTransactionPlan: TransactionPlanItem,
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse =
            transactionRepository.createTransactionHash(
                useMaxAmountFlag, toAddress, coinType,
                fromCoinAmount, fromTransactionPlan,
                utxosPerCoint[fromCoin].orEmpty()
            )
        return if (hashResponse.isRight) {
            val fromAddress = daoAccount.getItem(fromCoin).publicKey
            apiService.withdraw(
                (hashResponse as Either.Right).b,
                fromCoin,
                fromCoinAmount,
                fee,
                fromAddress,
                toAddress
            ).map { cache.update(it) }
        } else {
            hashResponse as Either.Left
        }
    }

    override suspend fun sendGift(
        useMaxAmountFlag: Boolean,
        amount: Double,
        coinCode: String,
        giftId: String?,
        phone: String,
        message: String?,
        fee: Double,
        toAddress: String,
        transactionPlanItem: TransactionPlanItem,
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(coinCode)
        val hashResponse = transactionRepository.createTransactionHash(
            useMaxAmountFlag, toAddress, coinType, amount,
            transactionPlanItem, utxosPerCoint[coinCode].orEmpty()
        )
        return if (hashResponse.isRight) {
            val item = daoAccount.getItem(coinCode)
            val fromAddress = item.publicKey
            val hash = (hashResponse as Either.Right).b
            if (coinCode == LocalCoinType.ETH.name || coinCode.isEthRelatedCoinCode()) {
                apiService.sendGift(
                    hash,
                    coinCode,
                    amount,
                    giftId,
                    phone,
                    message,
                    fee,
                    fromAddress,
                    toAddress
                )
            } else {
                apiService.sendGift(hash, coinCode, amount, giftId, phone, message)
            }.map { cache.update(it) }
        } else {
            hashResponse as Either.Left
        }
    }

    override suspend fun sellGetLimits(): Either<Failure, SellLimitsDataItem> =
        apiService.sellGetLimitsAsync()

    override suspend fun sellPreSubmit(
        smsCode: String,
        fromCoin: String,
        cryptoAmount: Double,
        toUsdAmount: Int
    ): Either<Failure, SellPreSubmitDataItem> {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCodeOld(smsCode)
        return if (smsCodeVerifyResponse.isRight) {
            apiService.sellPreSubmit(fromCoin, cryptoAmount, toUsdAmount)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    }

    override suspend fun sell(
        coin: String,
        coinAmount: Double,
        usdAmount: Int,
        fee: Double
    ): Either<Failure, Unit> {
        return apiService.sell(coin, coinAmount, usdAmount, getCoinByCode(coin).priceUsd, fee)
            .map { cache.update(it) }
    }

    override suspend fun exchange(
        useMaxAmountFlag: Boolean,
        fromCoinAmount: Double,
        toCoinAmount: Double,
        fromCoin: String,
        coinTo: String,
        fee: Double,
        transactionPlanItem: TransactionPlanItem
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val fromCoinItem = getCoinByCode(fromCoin)
        val toCoinItem = getCoinByCode(coinTo)
        val toAddress: String = if (fromCoin.isEthRelatedCoinCode()) {
            fromCoinItem.publicKey
        } else {
            fromCoinItem.details.walletAddress
        }
        val hashResponse = transactionRepository.createTransactionHash(
            useMaxAmountFlag, toAddress, coinType, fromCoinAmount,
            transactionPlanItem, utxosPerCoint[fromCoin].orEmpty()
        )
        return if (hashResponse.isRight) {
            val toAddressSend = fromCoinItem.details.walletAddress
            val fromAddress = daoAccount.getItem(fromCoin).publicKey
            val hash = (hashResponse as Either.Right).b
            apiService.exchange(
                fromCoinAmount, toCoinAmount,
                fromCoinItem, toCoinItem, hash,
                fee, fromAddress, toAddressSend
            ).map { cache.update(it) }
        } else {
            hashResponse as Either.Left
        }
    }

    override suspend fun tradeRecallTransactionComplete(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> {
        return apiService.submitRecall(coinCode, cryptoAmount).map { cache.update(it) }
    }

    override suspend fun tradeReserveTransactionCreate(
        useMaxAmountFlag: Boolean,
        coinCode: String,
        cryptoAmount: Double,
        transactionPlanItem: TransactionPlanItem
    ): Either<Failure, String> {
        val toAddress = getCoinByCode(coinCode).details.walletAddress
        val coinType = LocalCoinType.valueOf(coinCode)
        val hashResponse = transactionRepository.createTransactionHash(
            useMaxAmountFlag, toAddress, coinType, cryptoAmount,
            transactionPlanItem, utxosPerCoint[coinCode].orEmpty()
        )
        return when {
            hashResponse.isRight -> hashResponse as Either.Right
            else -> hashResponse as Either.Left
        }
    }

    override suspend fun tradeReserveTransactionComplete(
        coinCode: String,
        cryptoAmount: Double,
        hash: String,
        fee: Double,
        transactionPlanItem: TransactionPlanItem
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val fromAddress = coinItem.details.walletAddress
        val toAddress = coinItem.publicKey
        return apiService.submitReserve(coinCode, fromAddress, toAddress, cryptoAmount, fee, hash)
            .map { cache.update(it) }
    }

    override suspend fun stakeDetails(
        coinCode: String
    ): Either<Failure, StakeDetailsDataItem> = apiService.stakeDetails(coinCode)

    override suspend fun stakeCreate(
        coinCode: String,
        cryptoAmount: Double,
        transactionPlanItem: TransactionPlanItem,
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val fromAddress = coinItem.details.walletAddress
        val hash = transactionRepository.createTransactionStakeHash(
            cryptoAmount,
            coinItem.details.contractAddress,
            transactionPlanItem
        )
        val toAddress = coinItem.publicKey
        return apiService.stakeCreate(
            coinCode,
            fromAddress,
            toAddress,
            cryptoAmount,
            transactionPlanItem.nativeTxFee,
            hash
        ).map { cache.update(it) }
    }

    override suspend fun stakeCancel(
        coinCode: String,
        transactionPlanItem: TransactionPlanItem
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val hash = transactionRepository.createTransactionStakeCancelHash(
            0.0,
            coinItem.details.contractAddress,
            transactionPlanItem
        )
        val fromAddress = coinItem.details.walletAddress
        val toAddress = coinItem.publicKey
        val fee = transactionPlanItem.nativeTxFee
        return apiService.stakeCancel(
            coinCode, fromAddress, toAddress, 0.0, fee, hash
        ).map { cache.update(it) }
    }

    override suspend fun stakeWithdraw(
        coinCode: String,
        cryptoAmount: Double,
        transactionPlanItem: TransactionPlanItem
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val hash = transactionRepository.createTransactionUnStakeHash(
            cryptoAmount,
            coinItem.details.contractAddress,
            transactionPlanItem
        )
        val fromAddress = coinItem.details.walletAddress
        val toAddress = coinItem.publicKey
        val fee = transactionPlanItem.nativeTxFee
        return apiService.unStake(coinCode, fromAddress, toAddress, cryptoAmount, fee, hash)
            .map { cache.update(it) }
    }

    override suspend fun getTransferAddress(
        phone: String,
        coinCode: String
    ): Either<Failure, String> = apiService.getGiftAddress(coinCode, phone)

    private suspend fun getCoinByCode(coinCode: String): CoinDataItem =
        walletDao.getCoinByCode(coinCode).toDataItem()
}
