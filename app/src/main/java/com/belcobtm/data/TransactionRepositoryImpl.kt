package com.belcobtm.data

import android.location.Location
import com.belcobtm.data.core.TransactionHelper
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.toDataItem
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.inmemory.transactions.TransactionsInMemoryCache
import com.belcobtm.data.rest.transaction.TransactionApiService
import com.belcobtm.data.rest.transaction.response.hash.UtxoItemData
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.flatMapSuspend
import com.belcobtm.domain.map
import com.belcobtm.domain.tools.ToolsRepository
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.SellPreSubmitDataItem
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.StakeDetailsDataItem
import com.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isBtcCoin
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.tools.extensions.customPurpose
import com.belcobtm.presentation.tools.extensions.customXpubVersion
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

    private var utxosPerCoint: MutableMap<String, List<UtxoItemData>> = HashMap()

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

    override fun observeTransactions(): Flow<Map<String, TransactionDetailsDataItem>> =
        cache.observableData

    override suspend fun getSignedPlan(
        fromCoin: String,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        toAddress: String,
        useMaxAmountFlag: Boolean
    ): Either<Failure, SignedTransactionPlanItem> = transactionRepository.getSignedTransactionPlan(
        toAddress = toAddress,
        fromCoin = LocalCoinType.valueOf(fromCoin),
        fromCoinAmount = fromCoinAmount,
        fromTransactionPlan = fromTransactionPlan,
        useMaxAmountFlag = useMaxAmountFlag,
        utxos = utxosPerCoint[fromCoin].orEmpty()
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
                useMaxAmountFlag,
                toAddress,
                coinType,
                fromCoinAmount,
                fromTransactionPlan,
                utxosPerCoint[fromCoin].orEmpty()
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

    override suspend fun createTransactionToAddress(
        useMaxAmountFlag: Boolean,
        fromCoin: String,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        toAddress: String
    ): Either<Failure, String> {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse =
            transactionRepository.createTransactionHash(
                useMaxAmountFlag, toAddress, coinType, fromCoinAmount,
                fromTransactionPlan, utxosPerCoint[fromCoin].orEmpty()
            )
        return if (hashResponse.isRight) hashResponse as Either.Right else hashResponse as Either.Left
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
        fiatAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        price: Double
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse =
            transactionRepository.createTransactionHash(
                useMaxAmountFlag = useMaxAmountFlag,
                toAddress = toAddress,
                fromCoin = coinType,
                fromCoinAmount = fromCoinAmount,
                fromTransactionPlan = fromTransactionPlan,
                utxos = utxosPerCoint[fromCoin].orEmpty()
            )
        return if (hashResponse.isRight) {
            val fromAddress = daoAccount.getAccountByName(fromCoin).publicKey
            val transaction = apiService.withdraw(
                hash = (hashResponse as Either.Right).b,
                coinFrom = fromCoin,
                coinFromAmount = fromCoinAmount,
                fee = fee,
                fromAddress = fromAddress,
                toAddress = toAddress,
                price = price
            )
            if (transaction.isRight) {
                cache.update((transaction as Either.Right).b)
                Either.Right(Unit)
            } else transaction as Either.Left
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
        feePercent: Int?,
        fiatAmount: Double,
        toAddress: String,
        transactionPlanItem: TransactionPlanItem,
        location: Location
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(coinCode)
        val hashResponse = transactionRepository.createTransactionHash(
            useMaxAmountFlag, toAddress, coinType, amount,
            transactionPlanItem, utxosPerCoint[coinCode].orEmpty()
        )
        return if (hashResponse.isRight) {
            val item = daoAccount.getAccountByName(coinCode)
            val fromAddress = item.publicKey
            val hash = (hashResponse as Either.Right).b
            val transaction =
                if (coinCode == LocalCoinType.ETH.name || coinCode.isEthRelatedCoinCode()) {
                    apiService.sendTransfer(
                        hash = hash,
                        coinFrom = coinCode,
                        coinFromAmount = amount,
                        giftId = giftId,
                        phone = phone,
                        message = message,
                        fee = fee,
                        feePercent = feePercent,
                        fiatAmount = fiatAmount,
                        location = location,
                        fromAddress = fromAddress,
                        toAddress = toAddress,
                    )
                } else {
                    apiService.sendTransfer(
                        hash = hash,
                        coinFrom = coinCode,
                        coinFromAmount = amount,
                        giftId = giftId,
                        phone = phone,
                        message = message,
                        feePercent = feePercent,
                        fiatAmount = fiatAmount,
                        location = location
                    )
                }
            if (transaction.isRight) {
                cache.update((transaction as Either.Right).b)
                Either.Right(Unit)
            } else transaction as Either.Left
        } else {
            hashResponse as Either.Left
        }
    }

    override suspend fun sellPreSubmit(
        smsCode: String,
        fromCoin: String,
        cryptoAmount: Double,
        toUsdAmount: Int
    ): Either<Failure, SellPreSubmitDataItem> {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCodeOld(smsCode)
        return if (smsCodeVerifyResponse.isRight) {
            apiService.sellPreSubmit(
                coinFrom = fromCoin,
                coinFromAmount = cryptoAmount,
                usdToAmount = toUsdAmount
            )
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
        val transaction = apiService.atmSell(
            coin = coin,
            coinAmount = coinAmount,
            usdAmount = usdAmount,
            price = getCoinByCode(coin).priceUsd,
            fee = fee
        )
        return if (transaction.isRight) {
            cache.update((transaction as Either.Right).b)
            Either.Right(Unit)
        } else transaction as Either.Left
    }

    override suspend fun exchange(
        useMaxAmountFlag: Boolean,
        fromCoinAmount: Double,
        toCoinAmount: Double,
        fromCoin: String,
        coinTo: String,
        fee: Double,
        fiatAmount: Double,
        transactionPlanItem: TransactionPlanItem,
        location: Location
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
            useMaxAmountFlag = useMaxAmountFlag,
            toAddress = toAddress,
            fromCoin = coinType,
            fromCoinAmount = fromCoinAmount,
            fromTransactionPlan = transactionPlanItem,
            utxos = utxosPerCoint[fromCoin].orEmpty()
        )
        return if (hashResponse.isRight) {
            val toAddressSend = fromCoinItem.details.walletAddress
            val fromAddress = daoAccount.getAccountByName(fromCoin).publicKey
            val hash = (hashResponse as Either.Right).b
            val transaction = apiService.swap(
                coinFromAmount = fromCoinAmount,
                coinToAmount = toCoinAmount,
                coinFrom = fromCoinItem,
                coinTo = toCoinItem,
                hash = hash,
                fee = fee,
                fiatAmount = fiatAmount,
                fromAddress = fromAddress,
                toAddress = toAddressSend,
                location = location
            )
            if (transaction.isRight) {
                cache.update((transaction as Either.Right).b)
                Either.Right(Unit)
            } else transaction as Either.Left
        } else {
            hashResponse as Either.Left
        }
    }

    override suspend fun tradeRecallTransactionComplete(
        coinCode: String,
        cryptoAmount: Double,
        price: Double
    ): Either<Failure, Unit> {
        val transaction = apiService.submitRecall(
            coinCode = coinCode,
            cryptoAmount = cryptoAmount,
            price = price
        )
        return if (transaction.isRight) {
            cache.update((transaction as Either.Right).b)
            Either.Right(Unit)
        } else transaction as Either.Left
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
            useMaxAmountFlag = useMaxAmountFlag,
            toAddress = toAddress,
            fromCoin = coinType,
            fromCoinAmount = cryptoAmount,
            fromTransactionPlan = transactionPlanItem,
            utxos = utxosPerCoint[coinCode].orEmpty()
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
        transactionPlanItem: TransactionPlanItem,
        price: Double
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val toAddress = coinItem.details.walletAddress
        val fromAddress = coinItem.publicKey
        val transaction = apiService.submitReserve(
            coinCode = coinCode,
            fromAddress = fromAddress,
            toAddress = toAddress,
            cryptoAmount = cryptoAmount,
            fee = fee,
            hex = hash,
            price = price
        )
        return if (transaction.isRight) {
            cache.update((transaction as Either.Right).b)
            Either.Right(Unit)
        } else transaction as Either.Left
    }

    override suspend fun stakeDetails(
        coinCode: String
    ): Either<Failure, StakeDetailsDataItem> = apiService.stakeDetails(coinCode)

    override suspend fun stakeCreate(
        coinCode: String,
        cryptoAmount: Double,
        feePercent: Double,
        fiatAMount: Double,
        transactionPlanItem: TransactionPlanItem,
        location: Location
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val toAddress = coinItem.details.walletAddress
        val fromAddress = coinItem.publicKey
        val hash = transactionRepository.createTransactionStakeHash(
            cryptoAmount,
            coinItem.details.contractAddress,
            transactionPlanItem
        )
        return hash.flatMapSuspend {
            val transaction = apiService.stakeCreate(
                coinCode = coinCode,
                fromAddress = fromAddress,
                toAddress = toAddress,
                cryptoAmount = cryptoAmount,
                fee = transactionPlanItem.nativeTxFee,
                feePercent = feePercent,
                fiatAMount = fiatAMount,
                hex = it,
                location = location
            )
            if (transaction.isRight) {
                cache.update((transaction as Either.Right).b)
                Either.Right(Unit)
            } else transaction as Either.Left
        }
    }

    override suspend fun stakeCancel(
        coinCode: String,
        transactionPlanItem: TransactionPlanItem,
        location: Location
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
        return hash.flatMapSuspend {
            val transaction = apiService.stakeCancel(
                coinCode = coinCode,
                fromAddress = fromAddress,
                toAddress = toAddress,
                cryptoAmount = 0.0,
                fee = fee,
                hex = it,
                location = location
            )
            if (transaction.isRight) {
                cache.update((transaction as Either.Right).b)
                Either.Right(Unit)
            } else transaction as Either.Left
        }
    }

    override suspend fun stakeWithdraw(
        coinCode: String,
        cryptoAmount: Double,
        transactionPlanItem: TransactionPlanItem,
        location: Location
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
        return hash.flatMapSuspend {
            val transaction = apiService.unStake(
                coinCode = coinCode,
                fromAddress = fromAddress,
                toAddress = toAddress,
                cryptoAmount = cryptoAmount,
                fee = fee,
                hex = it,
                location = location
            )
            if (transaction.isRight) {
                cache.update((transaction as Either.Right).b)
                Either.Right(Unit)
            } else transaction as Either.Left
        }
    }

    override suspend fun getTransferAddress(
        phone: String,
        coinCode: String
    ): Either<Failure, String> = apiService.getGiftAddress(coinCode, phone)

    private suspend fun getCoinByCode(coinCode: String): CoinDataItem =
        walletDao.getCoinByCode(coinCode).toDataItem()
}
