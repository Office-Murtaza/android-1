package com.app.belcobtm.ui.main.coins.withdraw

import android.content.Context
import android.util.Log
import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.param.SendTransactionParam
import com.app.belcobtm.api.model.param.trx.Trx
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.*
import com.google.gson.Gson
import io.realm.Realm
import wallet.core.jni.*


class WithdrawPresenter : BaseMvpDIPresenterImpl<WithdrawContract.View, WithdrawDataManager>(),
    WithdrawContract.Presenter {
    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    private var coinAmount: Double? = null
    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()
    val mUserId = App.appContext().pref.getUserId().toString()

    private var mTransactionHash :String? = null
    private var mTransactionHashJson :String? = null
    private var mCoinDbModel: DbCryptoCoin? = null

    override fun getCoinTransactionHash(
        context: Context,
        coinId: String,
        toAddress: String,
        coinAmount: Double
    ) {
        this.coinAmount = coinAmount
        val seed = App.appContext().pref.getSeed()
        val hdWallet = HDWallet(seed, "")

        mCoinDbModel = coinModel.getCryptoCoin(realm, coinId)

        val coinType = if (mCoinDbModel != null) {
            CoinType.createFromValue(mCoinDbModel!!.coinTypeId)
        } else {
            mView?.showError(context.getString(R.string.wrong_crypto_coin_data))
            return
        }

//        Log.v("TO_ADDRESS", toAddress)
//        Log.v("CHANGE_ADDRESS", coinDbModel?.publicKey)

        getCoinTransactionHashObs(
            hdWallet,
            toAddress,
            coinType,
            coinAmount,
            mCoinDbModel,
            mDataManager
        )
            .flatMap { transactionHash ->
               // mTransactionHash = transactionHash

                if(CoinType.TRON == coinType){
                    mTransactionHashJson = transactionHash
                    mTransactionHash = null
                }else{
                    mTransactionHashJson = null
                    mTransactionHash = transactionHash
                }

               // Log.v("TRANSACTION_HEX", mTransactionHash)
                mDataManager.requestSmsCode(mUserId)
            }
            .subscribe({ response ->
                if (response.value!!.sent) {
                    mView?.openSmsCodeDialog()
                }
            }, { error -> checkError(error) })
    }


    override fun verifySmsCode(code: String) {
        mView?.showProgress(true)

        mDataManager.verifySmsCode(mUserId, code)
            .flatMap { res ->

                mDataManager.submitTx(
                    mUserId,
                    mCoinDbModel!!.coinType,
                    SendTransactionParam(
                        2,
                        coinAmount,
                        null,
                        null,
                        null,
                        mTransactionHash?.substring(2),
                        Gson().fromJson<Trx>(mTransactionHashJson, Trx::class.java)
                    )
                )



            }
            .subscribe(
                {
                    mView?.showProgress(false)
                    mView?.onTransactionDone()
                }
                ,
                { error: Throwable ->
                    mView?.showProgress(false)
                    //todo add handling verifySms error and sendHash error
                    if (error is ServerException && error.code != Const.ERROR_403) {
                        mView?.openSmsCodeDialog(error.errorMessage)
                    } else {
                        checkError(error)
                    }

                })
    }

/*

    private fun getCoinTransactionHashObs(
        hdWallet: HDWallet,
        toAddress: String,
        coinType: CoinType,
        coinAmount: Double
    ): Observable<String> {
        return when (coinType) {
            CoinType.XRP -> getXRPTransactionHashObs(toAddress, coinAmount)
            CoinType.BINANCE -> getBNBTransactionHashObs(toAddress, coinAmount)
            CoinType.BITCOIN,
            CoinType.BITCOINCASH,
            CoinType.LITECOIN -> getBTCTransactionHashObs(hdWallet, toAddress, coinType, coinAmount)
            CoinType.ETHEREUM -> getETHTransactionHashObs(toAddress, coinAmount)
            else -> Observable.just("")
        }
    }

    private fun getXRPTransactionHashObs(
        toAddress: String,
        coinAmount: Double
    ): Observable<String> {

        val cryptoToSubcoin =
            coinAmount * 1_000_000

        val fromAddress = mCoinDbModel?.publicKey
        val privateKey_ = PrivateKey(mCoinDbModel?.privateKey?.toHexByteArray())

        val signingInput = Ripple.SigningInput.newBuilder()
        signingInput.apply {
            account = fromAddress
            amount = cryptoToSubcoin.toLong()
            destination = toAddress
            fee = 200_000
            sequence = 1
            privateKey = ByteString.copyFrom(privateKey_.data())
        }

        val sign: Ripple.SigningOutput = RippleSigner.sign(signingInput.build())
        val signBytes = sign.encoded.toByteArray()

        val resTransactionHashStr = Numeric.toHexString(signBytes)
        return Observable.just(resTransactionHashStr)
    }

    private fun getETHTransactionHashObs(
        toAddress_: String,
        coinAmount: Double
    ): Observable<String> {

        val nonce_ = 1 // TODO: replace with actual api request

        val cryptoToSubcoin: BigDecimal =
            BigDecimal(coinAmount * 1_000_000_000_000_000_000)

        val nonceHex = String.format("%016llx", nonce_)
        val amountHex = String.format("%016llx", cryptoToSubcoin)
        val gasLimitHex = String.format("%016llx", "21000")//magic num
        val gasPriceHex = String.format("%016llx", "20_000_000_000")//magic num

        val signingInput = Ethereum.SigningInput.newBuilder()
        signingInput.apply {
            privateKey = mCoinDbModel?.privateKey?.toHexBytesInByteString()
            toAddress = toAddress_
            chainId = ByteString.copyFrom("0x1".toHexByteArray())
            nonce = ByteString.copyFrom(nonceHex.toHexByteArray())
            gasPrice = ByteString.copyFrom(gasPriceHex.toHexByteArray())
            gasLimit = ByteString.copyFrom(gasLimitHex.toHexByteArray())
            amount = ByteString.copyFrom(amountHex.toHexByteArray())
        }

        val sign: Ethereum.SigningOutput = EthereumSigner.sign(signingInput.build())

        val resTransactionHashStr = Numeric.toHexString(sign.toByteArray())
        return Observable.just(resTransactionHashStr)

    }

    private fun getBNBTransactionHashObs(
        toAddress: String,
        coinAmount: Double
    ): Observable<String> {

        val cryptoToSubcoin =
            coinAmount * 100_000_000

        val privateKey = PrivateKey(mCoinDbModel?.privateKey?.toHexByteArray())

        val publicKey = privateKey.getPublicKeySecp256k1(true)

        val signingInput = Binance.SigningInput.newBuilder()
        signingInput.chainId = "Binance-Chain-Tigris"
        signingInput.accountNumber = 280876//
        signingInput.sequence = 0

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

        val resTransactionHashStr = Numeric.toHexString(signBytes)
        return Observable.just(resTransactionHashStr)
    }

    private fun getBTCTransactionHashObs(
        hdWallet: HDWallet,
        toAddress: String,
        coinType: CoinType,
        coinAmount: Double
    ): Observable<String> {
        val extendedPublicKey =
            hdWallet.getExtendedPublicKey(coinType.getMyCustomPurpose(), coinType, coinType.getMyCustomXpubVersion())
//        Log.v("XPUB", extendedPublicKey)
        return mDataManager.getBTCUtxos(mUserId, mCoinDbModel!!.coinType, extendedPublicKey)
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

    private fun createBTCTransactionHash(
        toAddress: String,
        coinDbModel: DbCryptoCoin?,
        coinType: CoinType,
        utxos: ArrayList<UtxoItem>,
        coinAmount: Double
    ): String {
        val hdWallet = HDWallet(App.appContext().pref.getSeed(), "")
        val publicKeyFrom = coinDbModel?.publicKey

        val cryptoToSatoshi =
            coinAmount * 100_000_000//todo init cryptoToSatoshi course(100_000_000 for bitcoin) with data for each coin
        val amount: Long = cryptoToSatoshi.toLong()
//        Log.v("AMOUNT", amount.toString())

        val byteFee = getByteFee(coinDbModel?.coinType)

        val sngHash = getCryptoHash(coinType)
        val signerBuilder = Bitcoin.SigningInput.newBuilder()
            .setAmount(amount)
            .setHashType(sngHash)
            .setToAddress(toAddress)
            .setChangeAddress(publicKeyFrom)
            .setByteFee(byteFee.toLong())

        utxos.forEach {
            val privateKey = hdWallet.getKey(it.path)
            signerBuilder.addPrivateKey(ByteString.copyFrom(privateKey.data()))
//            Log.v("PRIVATE_KEY", org.web3j.utils.Numeric.toHexStringNoPrefix(privateKey.data()))
        }

        utxos.forEach {
            val redeemScript = BitcoinScript.buildForAddress(it.address,coinType)
            var keyHash = if (redeemScript.isPayToWitnessScriptHash) {
                redeemScript.matchPayToWitnessPublicKeyHash()
            } else {
                redeemScript.matchPayToPubkeyHash()
            }

            if (keyHash.isNotEmpty()) {
                var key = Numeric.toHexString(keyHash)
                val scriptByteString = ByteString.copyFrom(redeemScript.data())
                signerBuilder.putScripts(key, scriptByteString)
//                Log.v("SCRIPTS_KEY", key)
//                Log.v("SCRIPTS_VALUE", Numeric.toHexString(redeemScript.data()))
            }
        }

        utxos.forEachIndexed { currentIndex, utxo ->
            val hash = utxo.txid.toHexBytes()
            val reversedHash = hash.reversed().toByteArray()
            val reversedHashStr = ByteString.copyFrom(reversedHash)
//            Log.v("UTXO_OUTPOINT_HASH", Numeric.toHexString(reversedHash))

            val index = utxo.vout
//            Log.v("UTXO_OUTPOINT_INDEX", index.toString())

            val sequence = Int.MAX_VALUE - utxos.size + currentIndex
//            Log.v("UTXO_OUTPOINT_SEQUENCE", sequence.toString())

            val outpoint = Bitcoin.OutPoint.newBuilder()
                .setHash(reversedHashStr)
                .setIndex(index)
                .setSequence(sequence)
                .build()

            val amount_ = utxo.value.toLong()
//            Log.v("UTXO_AMOUNT", amount_.toString())

            val redeemScript = BitcoinScript.buildForAddress(utxo.address,coinType)
            val scriptByteString = ByteString.copyFrom(redeemScript.data())
//            Log.v("UTXO_SCRIPT", Numeric.toHexString(redeemScript.data()))

            val utxo0 = Bitcoin.UnspentTransaction.newBuilder()
                .setScript(scriptByteString)
                .setAmount(amount_)
                .setOutPoint(outpoint)
                .build()

            signerBuilder.addUtxo(utxo0)
        }

        val signer = BitcoinTransactionSigner(signerBuilder.build())
        val result = signer.sign()

        val output = result.getObjects(0).unpack(wallet.core.jni.proto.Bitcoin.SigningOutput::class.java)

        val resTransactionStr = Numeric.toHexString(output.encoded.toByteArray())
        return resTransactionStr
    }

    override fun validateAddress(coinId: String, walletAddress: String): Boolean {
        val coin = when (coinId) {
            "XRP" -> CoinType.XRP
            "BNB" -> CoinType.BINANCE
            "BTC" -> CoinType.BITCOIN
            "BCH" -> CoinType.BITCOINCASH
            "LTC" -> CoinType.LITECOIN
            else -> null
        }

        return coin?.validate(walletAddress) ?: false
    }


    override fun verifySmsCode(code: String) {
        mView?.showProgress(true)

        mDataManager.verifySmsCode(mUserId, code)
            .flatMap { res ->
                mDataManager.sendHash(mUserId, mCoinDbModel!!.coinType, mTransactionHash.substring(2))
            }
            .subscribe(
                {
                    mView?.showProgress(false)
                    mView?.onTransactionDone()
                }
                ,
                { error: Throwable ->
                    mView?.showProgress(false)
                    //todo add handling verifySms error and sendHash error
                    if (error is ServerException && error.code != Const.ERROR_403) {
                        mView?.openSmsCodeDialog(error.errorMessage)
                    } else {
                        checkError(error)
                    }

                })
    }

    override fun getTransactionFee(coinName: String): Double {
        return when (coinName) {
            "BTC" -> 0.0001
            "BCH" -> 0.0001
            "LTC" -> 0.00001
            else -> 4.0
        }
    }

    private fun getByteFee(coinName: String?): Int {
        return when (coinName) {
            "BTC" -> 40
            "BCH" -> 40
            "LTC" -> 4
            else -> 4
        }
    }

*/

}