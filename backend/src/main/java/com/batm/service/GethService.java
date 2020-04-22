package com.batm.service;

import com.batm.util.Constant;
import com.google.protobuf.ByteString;
import lombok.Getter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.jni.EthereumSigner;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Ethereum;
import java.math.BigDecimal;

@Getter
@Service
public class GethService {

    private final long GAS_PRICE = 50_000_000_000L;
    private final long GAS_LIMIT = 50_000;

    @Autowired
    private RestTemplate rest;

    @Autowired
    private WalletService walletService;

    @Value("${eth.node.url}")
    private String nodeUrl;

    @Value("${eth.explorer.url}")
    private String ethExplorerUrl;

    public BigDecimal getBalance(String address) {
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("method", "eth_getBalance");
            req.put("params", JSONArray.fromObject("[\"" + address + "\", \"latest\"]"));
            req.put("id", 1);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

            return new BigDecimal(Numeric.toBigInt(res.optString("result"))).divide(Constant.ETH_DIVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(String hex) {
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("method", "eth_sendRawTransaction");
            req.put("params", JSONArray.fromObject("[\"" + hex + "\"]"));
            req.put("id", 1);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

            return res.optString("result");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Long getGasPrice() {
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("method", "eth_gasPrice");
            req.put("params", new JSONArray());
            req.put("id", 73);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

            return Numeric.toBigInt(res.optString("result")).longValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return GAS_PRICE;
    }

    public Long getGasLimit() {
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("method", "eth_estimateGas");
            req.put("params", new JSONArray());
            req.put("id", 73);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

            return Numeric.toBigInt(res.optString("result")).longValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return GAS_LIMIT;
    }

    public BigDecimal getTxFee() {
        return new BigDecimal(getGasPrice()).multiply(new BigDecimal(getGasLimit())).divide(Constant.ETH_DIVIDER).stripTrailingZeros();
    }

    public Integer getNonce(String fromAddress) {
        return 0;
    }

    public String sign(String fromAddress, String toAddress, BigDecimal amount) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(fromAddress)) {
                privateKey = walletService.getPrivateKeyETH();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.getWallet().getKey(path);
            }

            Integer nonce = getNonce(fromAddress);
            Ethereum.SigningInput.Builder builder = Ethereum.SigningInput.newBuilder();

            builder.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKey.data()))));
            builder.setToAddress(toAddress);
            builder.setChainId(ByteString.copyFrom(Numeric.hexStringToByteArray("1")));
            builder.setNonce(ByteString.copyFrom(Numeric.hexStringToByteArray(Integer.toHexString(nonce))));
            builder.setGasPrice(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(getGasPrice()))));
            builder.setGasLimit(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(getGasLimit()))));
            builder.setAmount(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(amount.multiply(Constant.ETH_DIVIDER).longValue()))));

            Ethereum.SigningOutput output = EthereumSigner.sign(builder.build());

            return Numeric.toHexString(output.getEncoded().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}