package com.app.belcobtm.data

import com.app.belcobtm.data.core.TransactionHashHelper
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.rest.transaction.TransactionApiService
import com.app.belcobtm.data.websockets.base.model.WalletBalance
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.tools.ToolsRepository
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.*
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

class TransactionRepositoryImpl(
    private val apiService: TransactionApiService,
    private val walletObserver: WalletObserver,
    private val toolsRepository: ToolsRepository,
    private val transactionHashRepository: TransactionHashHelper,
    private val daoAccount: AccountDao
) : TransactionRepository {

    override suspend fun getTransactionList(
        coinCode: String,
        currentListSize: Int
    ): Either<Failure, Pair<Int, List<TransactionDataItem>>> = apiService.getTransactions(coinCode, currentListSize)

    override suspend fun createTransaction(
        fromCoin: String,
        fromCoinAmount: Double,
        isNeedSendSms: Boolean
    ): Either<Failure, String> {
        val toAddress = getCoinByCode(fromCoin)?.details?.walletAddress ?: ""
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse =
            transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
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

    override suspend fun withdraw(
        fromCoin: String,
        fromCoinAmount: Double,
        toAddress: String
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse =
            transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
        return if (hashResponse.isRight) {
            val fee = getCoinByCode(fromCoin)?.details?.txFee ?: 0.0
            val fromAddress = daoAccount.getItem(fromCoin).publicKey
            apiService.withdraw(
                (hashResponse as Either.Right).b,
                fromCoin,
                fromCoinAmount,
                fee,
                fromAddress,
                toAddress
            )
        } else {
            hashResponse as Either.Left
        }
    }

    override suspend fun sendGift(
        amount: Double,
        coinCode: String,
        giftId: String?,
        phone: String,
        message: String?
    ): Either<Failure, Unit> {
        val giftAddressResponse = apiService.getGiftAddress(coinCode, phone)
        return if (giftAddressResponse.isRight) {
            val coinType = LocalCoinType.valueOf(coinCode)
            val toAddress = (giftAddressResponse as Either.Right).b
            val hashResponse =
                transactionHashRepository.createTransactionHash(coinType, amount, toAddress)
            if (hashResponse.isRight) {
                val fee = getCoinByCode(coinCode)?.details?.txFee ?: 0.0
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
                }

            } else {
                hashResponse as Either.Left
            }
        } else {
            giftAddressResponse as Either.Left
        }
    }

    override suspend fun sellGetLimits(): Either<Failure, SellLimitsDataItem> = apiService.sellGetLimitsAsync()

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
        fromCoin: String,
        fromCoinAmount: Double
    ): Either<Failure, Unit> {
        val transactionResponse = createTransaction(fromCoin, fromCoinAmount, false)
        return if (transactionResponse.isRight) {
            val hash = (transactionResponse as Either.Right).b
            apiService.sell(fromCoinAmount, fromCoin, hash)
        } else {
            transactionResponse as Either.Left
        }
    }

    override suspend fun exchange(
        fromCoinAmount: Double,
        toCoinAmount: Double,
        fromCoin: String,
        coinTo: String
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val fromCoinItem = getCoinByCode(fromCoin)
        val toAddress: String = if (fromCoin.isEthRelatedCoinCode()) {
            fromCoinItem?.publicKey
        } else {
            fromCoinItem?.details?.walletAddress
        } ?: ""
        val hashResponse =
            transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
        return if (hashResponse.isRight) {
            val toAddressSend = fromCoinItem?.details?.walletAddress ?: ""
            val fee = fromCoinItem?.details?.txFee ?: 0.0
            val fromAddress = daoAccount.getItem(fromCoin).publicKey
            val hash = (hashResponse as Either.Right).b
            apiService.exchange(
                fromCoinAmount,
                toCoinAmount,
                fromCoin,
                coinTo,
                hash,
                fee,
                fromAddress,
                toAddressSend
            )
        } else {
            hashResponse as Either.Left
        }
    }

    override suspend fun tradeRecallTransactionComplete(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> {
        return apiService.submitRecall(coinCode, cryptoAmount)
    }

    override suspend fun tradeReserveTransactionCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, String> {
        val toAddress = getCoinByCode(coinCode)?.details?.walletAddress ?: ""
        val coinType = LocalCoinType.valueOf(coinCode)
        val hashResponse =
            transactionHashRepository.createTransactionHash(coinType, cryptoAmount, toAddress)
        return when {
            hashResponse.isRight -> hashResponse as Either.Right
            else -> hashResponse as Either.Left
        }
    }

    override suspend fun tradeReserveTransactionComplete(
        coinCode: String,
        cryptoAmount: Double,
        hash: String
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val fromAddress = coinItem?.details?.walletAddress ?: ""
        val toAddress = coinItem?.publicKey ?: ""
        val fee = coinItem?.details?.txFee ?: 0.0
        return apiService.submitReserve(coinCode, fromAddress, toAddress, cryptoAmount, fee, hash)
    }

    override suspend fun stakeDetails(
        coinCode: String
    ): Either<Failure, StakeDetailsDataItem> = apiService.stakeDetails(coinCode)

    override suspend fun stakeCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val transactionResponse = transactionHashRepository.createTransactionStakeHash(
            cryptoAmount,
            coinItem?.details?.walletAddress ?: ""
        )
        return if (transactionResponse.isRight) {
            val hash = (transactionResponse as Either.Right).b
            val fromAddress = coinItem?.details?.walletAddress ?: ""
            val toAddress = coinItem?.publicKey ?: ""
            val coinFee = coinItem?.details?.txFee ?: 0.0
            apiService.stakeCreate(coinCode, fromAddress, toAddress, cryptoAmount, coinFee, hash)
        } else {
            transactionResponse as Either.Left
        }
    }

    override suspend fun stakeCancel(
        coinCode: String
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val transactionResponse = transactionHashRepository.createTransactionStakeCancelHash(
            0.0,
            coinItem?.details?.walletAddress ?: ""
        )
        return if (transactionResponse.isRight) {
            val hash = (transactionResponse as Either.Right).b
            val fromAddress = coinItem?.details?.walletAddress ?: ""
            val toAddress = coinItem?.publicKey ?: ""
            val coinFee = coinItem?.details?.txFee ?: 0.0
            apiService.stakeCancel(coinCode, fromAddress, toAddress, 0.0, coinFee, hash)
        } else {
            transactionResponse as Either.Left
        }
    }

    override suspend fun stakeWithdraw(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> {
        val coinItem = getCoinByCode(coinCode)
        val transactionResponse = transactionHashRepository.createTransactionUnStakeHash(
            cryptoAmount,
            coinItem?.details?.walletAddress ?: ""
        )
        return if (transactionResponse.isRight) {
            val hash = (transactionResponse as Either.Right).b
            val fromAddress = coinItem?.details?.walletAddress ?: ""
            val toAddress = coinItem?.publicKey ?: ""
            val coinFee = coinItem?.details?.txFee ?: 0.0
            apiService.unStake(coinCode, fromAddress, toAddress, cryptoAmount, coinFee, hash)
        } else {
            transactionResponse as Either.Left
        }
    }

    override suspend fun getTransactionDetails(
        txId: String,
        coinCode: String
    ): Either<Failure, TransactionDetailsDataItem> = apiService.getTransactionDetails(txId, coinCode)

    override suspend fun checkXRPAddressActivated(address: String): Either<Failure, Boolean> {
        return apiService.getXRPAddressActivated(address)
    }

    private suspend fun getCoinByCode(coidCode: String): CoinDataItem? {
        return walletObserver.observe().receiveAsFlow()
            .filterIsInstance<WalletBalance.Balance>()
            .map { balance -> balance.data.coinList.firstOrNull { it.code == coidCode } }
            .firstOrNull()
    }
}
