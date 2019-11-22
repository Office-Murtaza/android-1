package com.batm.service;

import com.batm.dto.SubmitTransactionDTO;
import com.batm.util.AES;
import com.batm.util.Constant;
import com.batm.util.Util;
import com.google.protobuf.ByteString;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.utils.Numeric;
import wallet.core.jni.*;
import wallet.core.jni.proto.*;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class WalletService {

    static {
        System.loadLibrary("TrustWalletCore");
    }

    @Value("${wallet.seed}")
    private String walletSeed;

    @Value("${wallet.seed.key}")
    private String walletSeedKey;

    private HDWallet wallet = null;

    private String addressBTC = null;
    private String addressBCH = null;
    private String addressETH = null;
    private String addressLTC = null;
    private String addressBNB = null;
    private String addressXRP = null;
    private String addressTRX = null;

    private PrivateKey privateKeyBTC = null;
    private PrivateKey privateKeyETH = null;
    private PrivateKey privateKeyXRP = null;
    private PrivateKey privateKeyTRX = null;
    private PrivateKey privateKeyBNB = null;

    @PostConstruct
    public void init() {
        String seed = "garage become kid awake salon forget minimum snack crash broken leaf genius";

        //wallet = new HDWallet(AES.decrypt(walletSeed, walletSeedKey), "");
        wallet = new HDWallet(seed, "");

        privateKeyBTC = wallet.getKeyForCoin(CoinType.BITCOIN);
        String extPublicKeyBTC = wallet.getExtendedPublicKey(Purpose.BIP44, CoinType.BITCOIN, HDVersion.XPUB);
        PublicKey publicKeyBTC = HDWallet.getPublicKeyFromExtended(extPublicKeyBTC, "m/44'/0'/0'/0/0");
        addressBTC = new BitcoinAddress(publicKeyBTC, CoinType.BITCOIN.p2pkhPrefix()).description();

        PrivateKey privateKeyBCH = wallet.getKeyForCoin(CoinType.BITCOINCASH);
        addressBCH = CoinType.BITCOINCASH.deriveAddress(privateKeyBCH);

        privateKeyETH = wallet.getKeyForCoin(CoinType.ETHEREUM);
        addressETH = CoinType.ETHEREUM.deriveAddress(privateKeyETH);

        PrivateKey privateKeyLTC = wallet.getKeyForCoin(CoinType.LITECOIN);
        addressLTC = CoinType.LITECOIN.deriveAddress(privateKeyLTC);

        privateKeyBNB = wallet.getKeyForCoin(CoinType.BINANCE);
        addressBNB = CoinType.BINANCE.deriveAddress(privateKeyBNB);

        privateKeyXRP = wallet.getKeyForCoin(CoinType.XRP);
        addressXRP = CoinType.XRP.deriveAddress(privateKeyXRP);

        privateKeyTRX = wallet.getKeyForCoin(CoinType.TRON);
        addressTRX = CoinType.TRON.deriveAddress(privateKeyTRX);
    }

    public String getAddressBTC() {
        return addressBTC;
    }

    public String getAddressBCH() {
        return addressBCH;
    }

    public String getAddressETH() {
        return addressETH;
    }

    public String getAddressLTC() {
        return addressLTC;
    }

    public String getAddressBNB() {
        return addressBNB;
    }

    public String getAddressXRP() {
        return addressXRP;
    }

    public String getAddressTRX() {
        return addressTRX;
    }

    public SubmitTransactionDTO signBTC(CoinType coinType, String fromAddress, String toAddress, BigDecimal amount, BigDecimal fee, BigDecimal divider, List<JSONObject> utxos) {
        try {
            Bitcoin.SigningInput.Builder signerBuilder = Bitcoin.SigningInput.newBuilder();
            signerBuilder.setCoinType(coinType.value() == 2 ? 0 : coinType.value());
            signerBuilder.setAmount(amount.multiply(divider).longValue());
            signerBuilder.setByteFee(fee.multiply(divider).longValue());
            signerBuilder.setHashType(coinType == CoinType.BITCOINCASH ? 65 : 1);
            signerBuilder.setChangeAddress(fromAddress);
            signerBuilder.setToAddress(toAddress);

            utxos.forEach(e -> {
                PrivateKey privateKey = wallet.getKey(e.optString("path"));
                signerBuilder.addPrivateKey(ByteString.copyFrom(privateKey.data()));
            });

            utxos.forEach(e -> {
                BitcoinScript redeemScript = BitcoinScript.buildForAddress(e.optString("address"), coinType);
                byte[] keyHash = redeemScript.isPayToWitnessScriptHash() ? redeemScript.matchPayToWitnessPublicKeyHash() : redeemScript.matchPayToPubkeyHash();

                if (keyHash.length > 0) {
                    String key = Numeric.toHexString(keyHash);
                    ByteString scriptByteString = ByteString.copyFrom(redeemScript.data());
                    signerBuilder.putScripts(key, scriptByteString);
                }
            });

            for (int index = 0; index < utxos.size(); index++) {
                JSONObject utxo = utxos.get(index);

                byte[] hash = Numeric.hexStringToByteArray(utxo.optString("txid"));
                Collections.reverse(Arrays.asList(hash));

                Bitcoin.OutPoint.Builder outPointBuilder = Bitcoin.OutPoint.newBuilder();
                outPointBuilder.setHash(ByteString.copyFrom(hash));
                outPointBuilder.setIndex(utxo.optInt("vout"));
                outPointBuilder.setSequence(Integer.MAX_VALUE - utxos.size() + index);
                Bitcoin.OutPoint outPoint = outPointBuilder.build();

                BitcoinScript redeemScript = BitcoinScript.buildForAddress(utxo.optString("address"), coinType);
                ByteString scriptByteString = ByteString.copyFrom(redeemScript.data());

                Bitcoin.UnspentTransaction.Builder unspent = Bitcoin.UnspentTransaction.newBuilder();
                unspent.setScript(scriptByteString);
                unspent.setAmount(Long.parseLong(utxo.optString("value")));
                unspent.setOutPoint(outPoint);

                Bitcoin.UnspentTransaction unspentBuild = unspent.build();
                signerBuilder.addUtxo(unspentBuild);
            }

            BitcoinTransactionSigner signer = new BitcoinTransactionSigner(signerBuilder.build());
            Common.Result result = signer.sign();
            Bitcoin.SigningOutput output = result.getObjects(0).unpack(Bitcoin.SigningOutput.class);
            String hex = Numeric.toHexString(output.getEncoded().toByteArray());

            SubmitTransactionDTO dto = new SubmitTransactionDTO();
            dto.setHex(hex);

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public SubmitTransactionDTO signETH(String toAddress, BigDecimal amount, Integer nonce) {
        try {
            Ethereum.SigningInput.Builder builder = Ethereum.SigningInput.newBuilder();

            builder.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKeyETH.data()))));
            builder.setToAddress(toAddress);
            builder.setChainId(ByteString.copyFrom(Numeric.hexStringToByteArray("0x1")));

            builder.setNonce(ByteString.copyFrom(Numeric.hexStringToByteArray("0x" + Util.addLeadingZeroes(Integer.toHexString(nonce)))));
            builder.setGasPrice(ByteString.copyFrom(Numeric.hexStringToByteArray("0x" + Util.addLeadingZeroes(Long.toHexString(Constant.GAS_PRICE)))));
            builder.setGasLimit(ByteString.copyFrom(Numeric.hexStringToByteArray("0x" + Util.addLeadingZeroes(Long.toHexString(Constant.GAS_LIMIT)))));
            builder.setAmount(ByteString.copyFrom(Numeric.hexStringToByteArray("0x" + Util.addLeadingZeroes(Long.toHexString(amount.multiply(Constant.ETH_DIVIDER).longValue())))));

            Ethereum.SigningOutput output = EthereumSigner.sign(builder.build());
            String hex = "0x" + Numeric.toHexString(output.getEncoded().toByteArray());

            SubmitTransactionDTO dto = new SubmitTransactionDTO();
            dto.setHex(hex);

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public SubmitTransactionDTO signBNB(String toAddress, BigDecimal amount, Long accountNumber, Long sequence, String chainId) {
        try {
            Binance.SigningInput.Builder builder = Binance.SigningInput.newBuilder();
            builder.setChainId(chainId);
            builder.setAccountNumber(accountNumber);
            builder.setSequence(sequence);
            builder.setPrivateKey(ByteString.copyFrom(privateKeyBNB.data()));

            Binance.SendOrder.Token.Builder token = Binance.SendOrder.Token.newBuilder();
            token.setDenom("BNB");
            token.setAmount(amount.multiply(Constant.BNB_DIVIDER).longValue());

            Binance.SendOrder.Input.Builder input = Binance.SendOrder.Input.newBuilder();
            input.setAddress(ByteString.copyFrom(new CosmosAddress(HRP.BINANCE, privateKeyBNB.getPublicKeySecp256k1(true)).keyHash()));
            input.addAllCoins(Arrays.asList(token.build()));

            Binance.SendOrder.Output.Builder output = Binance.SendOrder.Output.newBuilder();
            output.setAddress(ByteString.copyFrom(new CosmosAddress(toAddress).keyHash()));
            output.addAllCoins(Arrays.asList(token.build()));

            Binance.SendOrder.Builder sendOrder = Binance.SendOrder.newBuilder();
            sendOrder.addAllInputs(Arrays.asList(input.build()));
            sendOrder.addAllOutputs(Arrays.asList(output.build()));

            builder.setSendOrder(sendOrder.build());

            Binance.SigningOutput sign = BinanceSigner.sign(builder.build());
            byte[] bytes = sign.getEncoded().toByteArray();
            String hex = Numeric.toHexString(bytes);

            SubmitTransactionDTO dto = new SubmitTransactionDTO();
            dto.setHex(hex);

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public SubmitTransactionDTO signXRP(String toAddress, BigDecimal amount, BigDecimal fee, Integer sequence) {
        try {
            Ripple.SigningInput.Builder builder = Ripple.SigningInput.newBuilder();
            builder.setAccount(addressXRP);
            builder.setDestination(toAddress);
            builder.setAmount(amount.multiply(Constant.XRP_DIVIDER).longValue());
            builder.setFee(fee.multiply(Constant.XRP_DIVIDER).longValue());
            builder.setSequence(sequence);
            builder.setPrivateKey(ByteString.copyFrom(privateKeyXRP.data()));

            Ripple.SigningOutput sign = RippleSigner.sign(builder.build());
            byte[] bytes = sign.getEncoded().toByteArray();
            String hex = Numeric.toHexString(bytes);

            SubmitTransactionDTO dto = new SubmitTransactionDTO();
            dto.setHex(hex);

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public SubmitTransactionDTO signTRX(String toAddress, BigDecimal amount, BigDecimal fee, JSONObject rawData) {
        try {
            Tron.BlockHeader.Builder headerBuilder = Tron.BlockHeader.newBuilder();
            headerBuilder.setNumber(rawData.optLong("number"));
            headerBuilder.setTimestamp(rawData.optLong("timestamp"));
            headerBuilder.setVersion(rawData.optInt("version"));
            headerBuilder.setParentHash(ByteString.copyFromUtf8(rawData.optString("parentHash")));
            headerBuilder.setWitnessAddress(ByteString.copyFromUtf8(rawData.optString("witness_address")));
            headerBuilder.setTxTrieRoot(ByteString.copyFromUtf8(rawData.optString("txTrieRoot")));

            Tron.TransferContract.Builder transferBuilder = Tron.TransferContract.newBuilder();
            transferBuilder.setOwnerAddress(addressTRX);
            transferBuilder.setToAddress(toAddress);
            transferBuilder.setAmount(amount.multiply(Constant.TRX_DIVIDER).longValue());

            Tron.Transaction.Builder transactionBuilder = Tron.Transaction.newBuilder();
            transactionBuilder.setTransfer(transferBuilder.build());
            transactionBuilder.setTimestamp(System.currentTimeMillis());
            transactionBuilder.setExpiration(transactionBuilder.getTimestamp() + 36000000);
            transactionBuilder.setFeeLimit(fee.multiply(Constant.TRX_DIVIDER).longValue());
            transactionBuilder.setBlockHeader(headerBuilder.build());

            Tron.SigningInput.Builder sign = Tron.SigningInput.newBuilder();
            sign.setTransaction(transactionBuilder.build());
            sign.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKeyTRX.data()))));

            Tron.SigningOutput output = TronSigner.sign(sign.build());

            SubmitTransactionDTO dto = new SubmitTransactionDTO();
            dto.setTrx(JSONObject.fromObject(output.getJson()));

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}