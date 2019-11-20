package com.app.belcobtm.mvp

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.BaseDataManager
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.response.BNBBlockResponse
import com.app.belcobtm.api.model.response.ETHResponse
import com.app.belcobtm.api.model.response.TronBlockResponse
import com.app.belcobtm.api.model.response.UtxoItem
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.di.component.DaggerPresenterComponent
import com.app.belcobtm.di.component.PresenterComponent
import com.app.belcobtm.di.module.PresenterModule
import com.app.belcobtm.util.*
import com.app.belcobtm.util.Optional
import com.google.protobuf.ByteString
import io.reactivex.Observable
import jnr.ffi.Struct
import wallet.core.jni.*
import wallet.core.jni.proto.*
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject


abstract class BaseMvpDIPresenterImpl<V : BaseMvpView, T : BaseDataManager> : BaseMvpPresenter<V> {

    protected var mView: V? = null

    protected val presenterComponent: PresenterComponent = DaggerPresenterComponent.builder()
        .presenterModule(PresenterModule())
        .build()

    protected abstract fun injectDependency()
    @Inject
    protected lateinit var mDataManager: T

    override fun attachView(view: V) {
        injectDependency()
        mView = view
    }

    override fun detachView() {
        mView = null
    }


    protected fun <T : Throwable> onError(exception: T) {
//        if (exception is ServerException) {
//            val messageStringId = exception.getMessageStringId()
//            if(messageStringId == -1)
//                mView?.showError(exception.message)
//            else
//                mView?.showError(messageStringId)
//        } else
        mView?.showError(exception.message)
    }

    protected fun checkError(error: Throwable) {
        mView?.showProgress(false)
        if (error is ServerException) {
            if (error.code == Const.ERROR_403) {
                mView?.onRefreshTokenFailed()
            } else {
                mView?.showError(error.errorMessage)
            }
        } else {
            mView?.showError(error.message)
        }
    }


    open fun getCoinTransactionHashObs(
        hdWallet: HDWallet,
        toAddress: String,
        coinType: CoinType?,
        coinAmount: Double,
        mCoinDbModel: DbCryptoCoin?,
        dataManager: WithdrawDataManager
    ): Observable<String> {

        val mUserId = App.appContext().pref.getUserId().toString()

        return when (coinType) {
            CoinType.XRP -> getXRPTransactionHashObs(
                toAddress, coinAmount,
                mCoinDbModel, dataManager, mUserId, coinType
            )
            CoinType.BINANCE -> getBNBTransactionHashObs(
                toAddress, coinAmount,
                dataManager, mUserId, mCoinDbModel, coinType
            )
            CoinType.BITCOIN,
            CoinType.BITCOINCASH,
            CoinType.LITECOIN -> getBTCTransactionHashObs(
                hdWallet, toAddress,
                coinType, coinAmount,
                dataManager, mUserId, mCoinDbModel
            )
            CoinType.TRON -> getTronTransactionHashObs(
                toAddress, coinAmount, dataManager,
                mUserId, mCoinDbModel, coinType
            )
            CoinType.ETHEREUM -> getETHTransactionHashObs(
                toAddress, coinAmount,
                dataManager, mUserId, mCoinDbModel
            )
            else -> Observable.just("")
        }
    }

    open fun getXRPTransactionHashObs(
        toAddress: String,
        coinAmount: Double,
        mCoinDbModel: DbCryptoCoin?,
        dataManager: WithdrawDataManager,
        mUserId: String,
        coinType: CoinType
    ): Observable<String> {


        val cryptoToSubcoin =
            coinAmount * 1_000_000

        val fromAddress = mCoinDbModel?.publicKey
        val privateKey_ = PrivateKey(mCoinDbModel?.privateKey?.toHexByteArray())


        return dataManager.getXRPBlockHeader(mUserId, mCoinDbModel?.publicKey ?: "")
            .map { resp ->
                createXRPTransaction(fromAddress, cryptoToSubcoin, toAddress, privateKey_, resp)
            }

    }

    private fun createXRPTransaction(
        fromAddress: String?,
        cryptoToSubcoin: Double,
        toAddress: String,
        privateKey_: PrivateKey,
        resp: Optional<BNBBlockResponse>
    ): String {
        val signingInput = Ripple.SigningInput.newBuilder()
        signingInput.apply {
            account = fromAddress
            amount = cryptoToSubcoin.toLong()
            destination = toAddress
            fee = 200_000

            sequence = resp.value?.sequence?.toInt() ?: 0
            privateKey = ByteString.copyFrom(privateKey_.data())
        }

        val sign: Ripple.SigningOutput = RippleSigner.sign(signingInput.build())
        val signBytes = sign.encoded.toByteArray()

        val resTransactionHashStr = Numeric.toHexString(signBytes)
        return resTransactionHashStr
    }


    open fun getTronTransactionHashObs(
        toAddress: String,
        coinAmount: Double,
        mDataManager: WithdrawDataManager,
        mUserId: String?,
        mCoinDbModel: DbCryptoCoin?,
        coinType: CoinType?

    ): Observable<String> {
        return mDataManager.getTronBlockHeader(mUserId, mCoinDbModel!!.coinType)
            .map { resp ->
                createTronTransactionHash(
                    toAddress,
                    mCoinDbModel,
                    coinType,
                    resp.value,
                    coinAmount
                )
            }
    }

    open fun createTronTransactionHash(
        toAddress: String, mCoinDbModel: DbCryptoCoin?,
        coinType: CoinType?, resp: TronBlockResponse?, coinAmount: Double
    ): String? {

        val cryptoToSubcoin =
            coinAmount * 1_000_000

        val fromAddress = mCoinDbModel?.publicKey

        val tronBlock = Tron.BlockHeader.newBuilder()

        tronBlock.number = resp?.blockHeader?.raw_data?.number ?: 0L
        tronBlock.timestamp = resp?.blockHeader?.raw_data?.timestamp ?: 0L
        tronBlock.version = resp?.blockHeader?.raw_data?.version ?: 0
        tronBlock.parentHash = resp?.blockHeader?.raw_data?.parentHash?.toHexBytesInByteString()
        tronBlock.witnessAddress =
            resp?.blockHeader?.raw_data?.witnessAddress?.toHexBytesInByteString()
        tronBlock.txTrieRoot = resp?.blockHeader?.raw_data?.txTrieRoot?.toHexBytesInByteString()

        val transferBuilder = Tron.TransferContract.newBuilder()

        transferBuilder.ownerAddress = fromAddress
        transferBuilder.toAddress = toAddress
        transferBuilder.amount = cryptoToSubcoin.toLong()


        val transaction = Tron.Transaction.newBuilder()
        transaction.transfer = transferBuilder.build()

        transaction.timestamp = Date().time

        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.HOUR, 10)
        val resHour = cal.time

        transaction.expiration = resHour.time ?: 0
        transaction.feeLimit = 1_000_000
        transaction.blockHeader = tronBlock.build()


        val signing = Tron.SigningInput.newBuilder()

        signing.transaction = transaction.build()
        signing.privateKey = mCoinDbModel?.privateKey?.toHexBytesInByteString()

        val output: Tron.SigningOutput = TronSigner.sign(signing.build())
        return output.json
    }

    open fun getETHTransactionHashObs(
        toAddress: String,
        coinAmount: Double,
        dataManager: WithdrawDataManager,
        mUserId: String,
        mCoinDbModel: DbCryptoCoin?
    ): Observable<String> {

        return dataManager.getETHNonce(mUserId, toAddress)
            .map { resp ->
                createETHTransactionHash(
                    toAddress,
                    mCoinDbModel!!,
                    resp.value,
                    coinAmount
                )
            }
    }

    /**
     * custom implementation of adding leading zeroes
     * for hex value (%016llx)
     */
     fun addLeadingZeroes(str:String):String?{
        var res:String =""
        if(str.length<64){
            var i = 0
            while ((64-str.length)>i){
                i++
                res+="0"
            }
            return res+str
        }
        return str
    }

    open fun createETHTransactionHash(
        _toAddress: String,
        mCoinDbModel: DbCryptoCoin,
        resp: ETHResponse?,
        coinAmount: Double
    ): String? {

        val cryptoToSubcoin: BigDecimal =
            BigDecimal(coinAmount * 1_000_000_000_000_000_000)

        //val nonceHex = String.format("%016llx", resp?.nonce)
        val nonsStr :String  = resp?.nonce?.toString(16) ?: ""

        val nonceHex = ByteString.copyFrom("0x${addLeadingZeroes(nonsStr)}".toHexByteArray())

        //val amountHex = String.format("%016llx", cryptoToSubcoin)
        val amountHex = ByteString.copyFrom("0x${addLeadingZeroes(cryptoToSubcoin.toLong().toString(16))}".toHexByteArray())

        // val gasLimitHex = String.format("%016llx", "21000")//magic num
        val gasLimitHex = ByteString.copyFrom("0x${addLeadingZeroes(21000.toString(16))}".toHexByteArray())

        //val gasPriceHex = String.format("%016llx", "20_000_000_000")//magic num
        val gasPriceHex = ByteString.copyFrom("0x${addLeadingZeroes(20_000_000_000.toString(16))}".toHexByteArray())


        val signingInput = Ethereum.SigningInput.newBuilder()
        signingInput.apply {
            privateKey = mCoinDbModel?.privateKey?.toHexBytesInByteString()
            toAddress = _toAddress
            chainId = ByteString.copyFrom("0x1".toHexByteArray())

            nonce = nonceHex
            gasPrice = gasPriceHex
            gasLimit = gasLimitHex
            amount = amountHex
        }

        val sign: Ethereum.SigningOutput = EthereumSigner.sign(signingInput.build())

        val resTransactionHashStr = Numeric.toHexString(sign.encoded.toByteArray())
        return "0x$resTransactionHashStr"
    }


    open fun getBNBTransactionHashObs(
        toAddress: String,
        coinAmount: Double,
        dataManager: WithdrawDataManager,
        mUserId: String,
        mCoinDbModel: DbCryptoCoin?,
        coinType: CoinType
    ): Observable<String> {

        val cryptoToSubcoin =
            coinAmount * 100_000_000

        val privateKey = PrivateKey(mCoinDbModel?.privateKey?.toHexByteArray())
        val publicKey = privateKey.getPublicKeySecp256k1(true)

        return dataManager.getBNBBlockHeader(mUserId, mCoinDbModel?.publicKey ?: "")
            .map { resp ->
                createBNBTransactionHash(
                    toAddress,
                    mCoinDbModel,
                    coinType,
                    resp.value,
                    cryptoToSubcoin,
                    privateKey, publicKey
                )
            }
    }

    open fun createBNBTransactionHash(
        toAddress: String,
        mCoinDbModel: DbCryptoCoin?,
        coinType: CoinType?,
        resp: BNBBlockResponse?,
        cryptoToSubcoin: Double,
        privateKey: PrivateKey,
        publicKey: PublicKey?
    ): String? {

        val signingInput = Binance.SigningInput.newBuilder()

        signingInput.chainId = "Binance-Chain-Tigris"

        signingInput.accountNumber = resp?.accountNumber ?: 0
        signingInput.sequence = resp?.sequence ?: 0

        signingInput.privateKey = ByteString.copyFrom(privateKey.data())

        val token = Binance.SendOrder.Token.newBuilder()
        token.denom = "BNB"
        token.amount = cryptoToSubcoin.toLong()

        val input = Binance.SendOrder.Input.newBuilder()
        input.address = ByteString.copyFrom(CosmosAddress(HRP.BINANCE, publicKey).keyHash())
        input.addAllCoins(listOf(token.build()))

        val output = Binance.SendOrder.Output.newBuilder()
        output.address = ByteString.copyFrom(CosmosAddress(toAddress).keyHash())
        output.addAllCoins(listOf(token.build()))

        val sendOrder = Binance.SendOrder.newBuilder()
        sendOrder.addAllInputs(listOf(input.build()))
        sendOrder.addAllOutputs(listOf(output.build()))

        signingInput.sendOrder = sendOrder.build()

        val sign: Binance.SigningOutput = BinanceSigner.sign(signingInput.build())
        val signBytes = sign.encoded.toByteArray()

        return Numeric.toHexString(signBytes)

    }

    open fun getBTCTransactionHashObs(
        hdWallet: HDWallet,
        toAddress: String,
        coinType: CoinType,
        coinAmount: Double,
        dataManager: WithdrawDataManager,
        mUserId: String,
        mCoinDbModel: DbCryptoCoin?
    ): Observable<String> {
        val extendedPublicKey =
            hdWallet.getExtendedPublicKey(
                coinType.getMyCustomPurpose(),
                coinType,
                coinType.getMyCustomXpubVersion()
            )

        return dataManager.getBTCUtxos(mUserId, mCoinDbModel!!.coinType, extendedPublicKey)
            .map { utxosResponse ->
                createBTCTransactionHash(
                    toAddress,
                    mCoinDbModel,
                    coinType,
                    utxosResponse.value!!.utxoList,
                    coinAmount
                )
            }
    }

    open fun createBTCTransactionHash(
        toAddress: String,
        coinDbModel: DbCryptoCoin?,
        coinType: CoinType,
        utxos: ArrayList<UtxoItem>,
        coinAmount: Double
    ): String {
        val hdWallet = HDWallet(App.appContext().pref.getSeed(), "")
        val publicKeyFrom = coinDbModel?.publicKey

        val cryptoToSatoshi =
            coinAmount * 100_000_000

        val amount: Long = cryptoToSatoshi.toLong()

        val byteFee = getByteFee(coinDbModel?.coinType)

        val sngHash = TWBitcoinSigHashType.getCryptoHash(coinType)

        val cointypeValue = if(coinType.value()==2) 0 else coinType.value()
        val signerBuilder = Bitcoin.SigningInput.newBuilder()
            .setAmount(amount)
            .setHashType(sngHash)
            .setToAddress(toAddress)
            .setChangeAddress(publicKeyFrom)
            .setByteFee(byteFee.toLong())
            .setCoinType(cointypeValue)

        utxos.forEach {
            val privateKey = hdWallet.getKey(it.path)
            signerBuilder.addPrivateKey(ByteString.copyFrom(privateKey.data()))
        }

        utxos.forEach {
            val redeemScript = BitcoinScript.buildForAddress(it.address, coinType)
            var keyHash = if (redeemScript.isPayToWitnessScriptHash) {
                redeemScript.matchPayToWitnessPublicKeyHash()
            } else {
                redeemScript.matchPayToPubkeyHash()
            }

            if (keyHash.isNotEmpty()) {
                var key = Numeric.toHexString(keyHash)
                val scriptByteString = ByteString.copyFrom(redeemScript.data())
                signerBuilder.putScripts(key, scriptByteString)
            }
        }

        utxos.forEachIndexed { currentIndex, utxo ->
            val hash = utxo.txid.toHexBytes()
            val reversedHash = hash.reversed().toByteArray()
            val reversedHashStr = ByteString.copyFrom(reversedHash)

            val index = utxo.vout

            val sequence = Int.MAX_VALUE - utxos.size + currentIndex

            val outpoint = Bitcoin.OutPoint.newBuilder()
                .setHash(reversedHashStr)
                .setIndex(index)
                .setSequence(sequence)
                .build()

            val amount_ = utxo.value.toLong()

            val redeemScript = BitcoinScript.buildForAddress(utxo.address, coinType)
            val scriptByteString = ByteString.copyFrom(redeemScript.data())

            val utxo0 = Bitcoin.UnspentTransaction.newBuilder()
                .setScript(scriptByteString)
                .setAmount(amount_)
                .setOutPoint(outpoint)
                .build()

            signerBuilder.addUtxo(utxo0)
        }

        val signer = BitcoinTransactionSigner(signerBuilder.build())
        val result = signer.sign()

        val output =
            result.getObjects(0).unpack(wallet.core.jni.proto.Bitcoin.SigningOutput::class.java)

        val resTransactionStr = Numeric.toHexString(output.encoded.toByteArray())
        return resTransactionStr
    }


    open fun validateAddress(coinId: String, walletAddress: String): Boolean {
        val coin = when (coinId) {
            "TRX" -> CoinType.TRON
            "XRP" -> CoinType.XRP
            "BNB" -> CoinType.BINANCE
            "BTC" -> CoinType.BITCOIN
            "BCH" -> CoinType.BITCOINCASH
            "LTC" -> CoinType.LITECOIN
            "ETH" -> CoinType.ETHEREUM
            else -> null
        }

        return coin?.validate(walletAddress) ?: false
    }


    /**
     * bitcoin: 0.0004
    bitcoinCash: 0.0004
    litecoin: 0.00004
    ethereum: 0.00042
    binance: 0.001
    tron: 1
    ripple: 0.00002
     */
    open fun getTransactionFee(coinName: String): Double {
        return when (coinName) {
            "BTC" -> 0.0004
            "BCH" -> 0.0004
            "LTC" -> 0.00004
            "ETH" -> 0.00042
            "BNB" -> 0.001
            "TRX" -> 1.0
            "XRP" -> 0.00002
            else -> 4.0
        }
    }

    open fun getByteFee(coinName: String?): Int {
        return when (coinName) {
            "BTC" -> 40
            "BCH" -> 40
            "LTC" -> 4
            else -> 4
        }
    }


}