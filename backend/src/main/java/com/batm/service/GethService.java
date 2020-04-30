package com.batm.service;

import com.batm.dto.NodeTransactionsDTO;
import com.batm.dto.TransactionDetailsDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.dto.TxListDTO;
import com.batm.model.GethBlock;
import com.batm.model.GethTx;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.util.Constant;
import com.batm.util.TxUtil;
import com.google.protobuf.ByteString;
import lombok.Getter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.utils.Numeric;
import wallet.core.jni.EthereumSigner;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Ethereum;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Getter
@Service
@EnableScheduling
public class GethService {

    private final long GAS_PRICE = 50_000_000_000L;
    private final long GAS_LIMIT = 50_000;

    private final int START_BLOCK = 9_970_000;
    private final int MAX_BLOCK_COUNT = 50;

    @Autowired
    private RestTemplate rest;

    @Autowired
    private WalletService walletService;

    @Autowired
    private MongoTemplate mongo;

    @Value("${eth.node.url}")
    private String nodeUrl;

    @Value("${eth.explorer.url}")
    private String ethExplorerUrl;

    @Value("${eth.node.store.enabled:false}")
    private Boolean storeEnabled;

    @Scheduled(cron = "0 */10 * * * *") // every 10 minutes
    public void storeTxs() {
        long start = System.currentTimeMillis();

        if (storeEnabled) {
            GethBlock block = mongo.exists(new Query(), GethBlock.class) ? mongo.findOne(new Query(), GethBlock.class) : new GethBlock(START_BLOCK);
            BigInteger lastBlockNumber = getLastBlockNumber();

            if (block.getLastSuccessBlock() < lastBlockNumber.intValue()) {
                List<GethTx> gethTxs = new ArrayList<>();
                int n = Math.min(MAX_BLOCK_COUNT, lastBlockNumber.intValue() - block.getLastSuccessBlock());
                int toBlockNumber = block.getLastSuccessBlock() + n;

                for (int i = block.getLastSuccessBlock() + 1; i < toBlockNumber + 1; i++) {
                    JSONObject blockJson = getBlockByNumber(BigInteger.valueOf(i));
                    JSONArray txs = blockJson.optJSONArray("transactions");

                    for (int j = 0; j < txs.size(); j++) {
                        JSONObject json = txs.getJSONObject(j);

                        BigDecimal amount = new BigDecimal(Numeric.toBigInt(json.optString("value")))
                                .divide(Constant.ETH_DIVIDER)
                                .stripTrailingZeros();

                        BigDecimal fee = new BigDecimal(Numeric.toBigInt(json.optString("gasPrice")))
                                .multiply(new BigDecimal(Numeric.toBigInt(json.optString("gas"))))
                                .divide(Constant.ETH_DIVIDER)
                                .stripTrailingZeros();

                        gethTxs.add(GethTx.builder()
                                .txId(json.optString("hash"))
                                .blockNumber(i)
                                .fromAddress(json.optString("from"))
                                .toAddress(json.optString("to"))
                                .amount(amount)
                                .fee(fee)
                                .blockTime(Numeric.toBigInt(blockJson.optString("timestamp")).longValue())
                                .build());
                    }

                    mongo.remove(new Query(Criteria.where("blockNumber").is(i)), GethTx.class);
                }

                mongo.insertAll(gethTxs);

                mongo.upsert(new Query(), new Update().set("lastSuccessBlock", toBlockNumber), GethBlock.class);

                System.out.println("**** storeTxs: " + (System.currentTimeMillis() - start));
            }
        }
    }

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
        return GAS_LIMIT;
    }

    public BigDecimal getTxFee() {
        return new BigDecimal(getGasPrice()).multiply(new BigDecimal(getGasLimit())).divide(Constant.ETH_DIVIDER).stripTrailingZeros();
    }

    public Integer getNonce(String address) {
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("method", "eth_getTransactionCount");
            req.put("params", JSONArray.fromObject("[\"" + address + "\", \"latest\"]"));
            req.put("id", 1);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

            return Numeric.toBigInt(res.optString("result")).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransactionDetailsDTO getTransaction(String txId, String address) {
        TransactionDetailsDTO dto = new TransactionDetailsDTO();
        dto.setTxId(txId);
        dto.setLink(ethExplorerUrl + "/" + txId);

        try {
            GethTx gethTx = mongo.findOne(new Query(Criteria.where("_id").is(txId)), GethTx.class);

            if (gethTx != null) {
                String fromAddress = gethTx.getFromAddress();
                String toAddress = gethTx.getToAddress();

                dto.setType(TransactionType.getType(fromAddress, toAddress, address));
                dto.setCryptoAmount(gethTx.getAmount());
                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setCryptoFee(gethTx.getFee());
                dto.setStatus(TransactionStatus.COMPLETE);
                dto.setDate2(new Date(gethTx.getBlockTime() * 1000));
            } else {
                JSONObject txByHash = getTransactionByHash(txId);

                if (txByHash != null) {
                    String fromAddress = txByHash.optString("from");
                    String toAddress = txByHash.containsKey("to") ? txByHash.optString("to") : getTransactionReceipt(txId).optString("to");

                    BigDecimal amount = new BigDecimal(Numeric.toBigInt(txByHash.optString("value")))
                            .divide(Constant.ETH_DIVIDER)
                            .stripTrailingZeros();

                    BigDecimal fee = new BigDecimal(Numeric.toBigInt(txByHash.optString("gasPrice")))
                            .multiply(new BigDecimal(Numeric.toBigInt(txByHash.optString("gas"))))
                            .divide(Constant.ETH_DIVIDER)
                            .stripTrailingZeros();

                    dto.setType(TransactionType.getType(fromAddress, toAddress, address));
                    dto.setCryptoAmount(amount);
                    dto.setFromAddress(fromAddress);
                    dto.setToAddress(toAddress);
                    dto.setCryptoFee(fee);
                    dto.setStatus(TransactionStatus.PENDING);
                    dto.setDate2(new Date());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        try {
            Map<String, TransactionDetailsDTO> map = getNodeTransactions(address).getMap();

            return TxUtil.buildTxs(map, startIndex, limit, txDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    public NodeTransactionsDTO getNodeTransactions(String address) {
        try {
            Map<String, TransactionDetailsDTO> map = new HashMap<>();
            List<GethTx> gethTxs = mongo.find(new Query(Criteria.where("fromAddress").is(address).orOperator(Criteria.where("toAddress").is(address))), GethTx.class);

            for (GethTx gethTx : gethTxs) {
                TransactionDetailsDTO dto = new TransactionDetailsDTO();

                String fromAddress = gethTx.getFromAddress();
                String toAddress = gethTx.getToAddress();

                dto.setType(TransactionType.getType(fromAddress, toAddress, address));
                dto.setCryptoAmount(gethTx.getAmount());
                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setCryptoFee(gethTx.getFee());
                dto.setStatus(TransactionStatus.COMPLETE);
                dto.setDate1(new Date(gethTx.getBlockTime() * 1000));

                map.put(gethTx.getTxId(), dto);
            }

            return new NodeTransactionsDTO(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new NodeTransactionsDTO();
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

    private BigInteger getLastBlockNumber() {
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("method", "eth_blockNumber");
            req.put("params", new JSONArray());
            req.put("id", 1);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

            return Numeric.toBigInt(res.optString("result"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject getBlockByNumber(BigInteger blockNumber) {
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("method", "eth_getBlockByNumber");
            req.put("params", JSONArray.fromObject("[\"" + Numeric.toHexStringWithPrefix(blockNumber) + "\", true]"));
            req.put("id", 1);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

            return res.optJSONObject("result");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject getTransactionByHash(String txId) {
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("method", "eth_getTransactionByHash");
            req.put("params", JSONArray.fromObject("[\"" + txId + "\"]"));
            req.put("id", 1);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

            return res.optJSONObject("result");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject getTransactionReceipt(String txId) {
        try {
            JSONObject req = new JSONObject();
            req.put("jsonrpc", "2.0");
            req.put("method", "eth_getTransactionReceipt");
            req.put("params", JSONArray.fromObject("[\"" + txId + "\"]"));
            req.put("id", 1);

            JSONObject res = rest.postForObject(nodeUrl, req, JSONObject.class);

            return res.optJSONObject("result");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}