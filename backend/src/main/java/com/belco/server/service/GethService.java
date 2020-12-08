package com.belco.server.service;

import com.belco.server.dto.*;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.token.CATM;
import com.belco.server.token.USDT;
import com.belco.server.util.TxUtil;
import com.belco.server.util.Util;
import com.google.protobuf.ByteString;
import com.mongodb.BasicDBList;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOneModel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Numeric;
import wallet.core.java.AnySigner;
import wallet.core.jni.CoinType;
import wallet.core.jni.EthereumAbi;
import wallet.core.jni.EthereumAbiFunction;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Ethereum;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Getter
@Service
@EnableScheduling
public class GethService {

    public static final BigDecimal ETH_DIVIDER = BigDecimal.valueOf(1_000_000_000_000_000_000L);

    private static final int START_BLOCK = 10290000;
    private static final int MAX_BLOCK_COUNT = 500;
    private static final long WATCH_TIME = 1800000;

    private static final String ADDRESS_COLL = "eth_address";
    private static final String BLOCK_COLL = "eth_block";
    private static final String ETH_TX_COLL = "eth_transaction";
    private static final String TOKEN_TX_COLL = "token_transaction";

    private static String nodeUrl;
    public static String explorerUrl;
    private static long ethInitialGasLimit;
    private static String catmContractAddress;
    private static String usdtContractAddress;

    private static RestTemplate rest;
    private static MongoTemplate mongo;
    private static CacheService cacheService;
    private static WalletService walletService;

    public static Web3j web3;
    public static CATM catm;
    public static USDT usdt;

    public GethService(@Value("${eth.node.url}") String nodeUrl,
                       @Value("${eth.explorer.url}") String explorerUrl,
                       @Value("${eth.initial.gas-limit}") long ethInitialGasLimit,
                       @Value("${catm.contract.address}") String catmContractAddress,
                       @Value("${usdt.contract.address}") String usdtContractAddress,
                       @Autowired RestTemplate rest,
                       @Autowired MongoTemplate mongo,
                       @Autowired CacheService cacheService,
                       @Autowired WalletService walletService) {

        GethService.nodeUrl = nodeUrl;
        GethService.explorerUrl = explorerUrl;
        GethService.ethInitialGasLimit = ethInitialGasLimit;
        GethService.catmContractAddress = catmContractAddress;
        GethService.usdtContractAddress = usdtContractAddress;
        GethService.rest = rest;
        GethService.mongo = mongo;
        GethService.cacheService = cacheService;
        GethService.walletService = walletService;

        try {
            web3 = Web3j.build(new HttpService(nodeUrl));

            ContractGasProvider gasProvider = new StaticGasProvider(BigInteger.valueOf(getFastGasPrice()), BigInteger.valueOf(ethInitialGasLimit));

            catm = CATM.load(catmContractAddress, web3,
                    Credentials.create(Numeric.toHexString(walletService.getCoinsMap().get(CoinType.ETHEREUM).getPrivateKey().data())), gasProvider);

            usdt = USDT.load(usdtContractAddress, web3,
                    Credentials.create(Numeric.toHexString(walletService.getCoinsMap().get(CoinType.ETHEREUM).getPrivateKey().data())), gasProvider);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum ERC20 {
        CATM {
            @Override
            public String getContractAddress() {
                return catmContractAddress;
            }

            @Override
            public BigDecimal getBalance(String address) {
                try {
                    return new BigDecimal(catm.balanceOf(address).send()).divide(ETH_DIVIDER);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return BigDecimal.ZERO;
            }
        },
        USDT {
            @Override
            public String getContractAddress() {
                return usdtContractAddress;
            }

            @Override
            public BigDecimal getBalance(String address) {
                try {
                    return new BigDecimal(usdt.balanceOf(address).send()).divide(ETH_DIVIDER);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return BigDecimal.ZERO;
            }
        };

        public abstract String getContractAddress();

        public abstract BigDecimal getBalance(String address);
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void storeNodeTransactions() {
        try {
            int lastSuccessBlock = mongo.getCollection(BLOCK_COLL).countDocuments() > 0 ? mongo.getCollection(BLOCK_COLL).find().first().getInteger("lastSuccessBlock") : START_BLOCK;
            int lastBlockNumber = web3.ethBlockNumber().send().getBlockNumber().intValue();

            if (lastSuccessBlock < lastBlockNumber) {
                int n = Math.min(MAX_BLOCK_COUNT, lastBlockNumber - lastSuccessBlock);
                int toBlockNumber = lastSuccessBlock + n;

                for (int i = lastSuccessBlock + 1; i <= toBlockNumber; i++) {
                    List<UpdateOneModel<Document>> ethTxs = new ArrayList<>();
                    List<UpdateOneModel<Document>> tokenTxs = new ArrayList<>();

                    EthBlock.Block block = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true).send().getBlock();

                    block.getTransactions().stream().forEach(e -> {
                        org.web3j.protocol.core.methods.response.Transaction tx = ((EthBlock.TransactionObject) e.get()).get();
                        long timestamp = block.getTimestamp().longValue() * 1000;

                        fetchEthTransaction(tx, timestamp, ethTxs, tokenTxs);
                    });

                    bulkWrite(ETH_TX_COLL, ethTxs);
                    bulkWrite(TOKEN_TX_COLL, tokenTxs);

                    mongo.getCollection(BLOCK_COLL).findOneAndUpdate(
                            new Document("lastSuccessBlock", new Document("$exists", true)),
                            new Document("$set", new Document("lastSuccessBlock", i).append("timestamp", System.currentTimeMillis())),
                            new FindOneAndUpdateOptions().upsert(true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void updatePendingTransactions() {
        try {
            List<UpdateOneModel<Document>> ethTxs = new ArrayList<>();
            List<UpdateOneModel<Document>> tokenTxs = new ArrayList<>();

            mongo.getCollection(ETH_TX_COLL).find(new Document("status", TransactionStatus.PENDING.getValue()).append("timestamp", new Document("$gte", System.currentTimeMillis() - WATCH_TIME))).limit(10).into(new ArrayList<>()).stream().forEach(d -> {
                org.web3j.protocol.core.methods.response.Transaction tx = getTransactionByHash(d.getString("txId"));

                fetchEthTransaction(tx, System.currentTimeMillis(), ethTxs, tokenTxs);
            });

            bulkWrite(ETH_TX_COLL, ethTxs);
            bulkWrite(TOKEN_TX_COLL, tokenTxs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BigDecimal getBalance(String address) {
        try {
            EthGetBalance getBalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();

            return new BigDecimal(getBalance.getBalance()).divide(ETH_DIVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(SubmitTransactionDTO dto) {
        try {
            String txId = web3.ethSendRawTransaction(dto.getHex()).send().getTransactionHash();

            if (StringUtils.isNotBlank(txId)) {
                addPendingTransaction(txId, dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());
                return txId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String submitTransaction(ERC20 token, SubmitTransactionDTO dto) {
        try {
            String txId = web3.ethSendRawTransaction(dto.getHex()).send().getTransactionHash();

            if (StringUtils.isNotBlank(txId)) {
                addPendingTransaction(token, txId, dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());

                return txId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public BigDecimal getTxFee(Long gasLimit, Long gasPrice) {
        return new BigDecimal(gasLimit).multiply(new BigDecimal(gasPrice)).divide(ETH_DIVIDER).stripTrailingZeros();
    }

    public Integer getNonce(String address) {
        try {
            return web3.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send().getTransactionCount().intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransactionStatus getTransactionStatus(String txId) {
        try {
            Optional<TransactionReceipt> receiptOptional = web3.ethGetTransactionReceipt(txId).send().getTransactionReceipt();

            if (receiptOptional.isPresent()) {
                return getTransactionStatus(receiptOptional.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionStatus.FAIL;
    }

    public TransactionStatus getTransactionStatus(TransactionReceipt receipt) {
        try {
            int status = Numeric.toBigInt(receipt.getStatus()).intValue();

            if (status == 0) {
                return TransactionStatus.FAIL;
            } else {
                return TransactionStatus.COMPLETE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TransactionStatus.PENDING;
    }

    public TransactionDetailsDTO getTransaction(String txId, String address) {
        Document search = new Document("txId", txId.toLowerCase());

        return getTransactionFromDB(ETH_TX_COLL, search, address.toLowerCase());
    }

    public TransactionDetailsDTO getTransaction(ERC20 token, String txId, String address) {
        return getTransactionFromDB(TOKEN_TX_COLL, new Document("txId", txId).append("token", token.name()), address.toLowerCase());
    }

    public TransactionListDTO getTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        BasicDBList or = new BasicDBList();
        or.add(new Document("fromAddress", address));
        or.add(new Document("toAddress", address));

        return buildTransactionList(ETH_TX_COLL, new Document("$or", or), address.toLowerCase(), startIndex, limit, txDTO);
    }

    public TransactionListDTO getTransactionList(ERC20 token, String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        BasicDBList or = new BasicDBList();
        or.add(new Document("fromAddress", address));
        or.add(new Document("toAddress", address));

        BasicDBList and = new BasicDBList();
        and.add(new Document("$or", or));
        and.add(new Document("token", token.name()));

        return buildTransactionList(TOKEN_TX_COLL, new Document("$and", and), address.toLowerCase(), startIndex, limit, txDTO);
    }

    public NodeTransactionsDTO getNodeTransactions(String address) {
        BasicDBList or = new BasicDBList();
        or.add(new Document("fromAddress", address.toLowerCase()));
        or.add(new Document("toAddress", address.toLowerCase()));

        return getNodeTransactionsFromDB(ETH_TX_COLL, new Document("$or", or), address.toLowerCase());
    }

    public NodeTransactionsDTO getNodeTransactions(ERC20 token, String address) {
        BasicDBList or = new BasicDBList();
        or.add(new Document("fromAddress", address));
        or.add(new Document("toAddress", address));

        BasicDBList and = new BasicDBList();
        and.add(new Document("$or", or));
        and.add(new Document("token", token.name()));

        return getNodeTransactionsFromDB(ETH_TX_COLL, new Document("$and", and), address.toLowerCase());
    }

    public String sign(String fromAddress, String toAddress, BigDecimal amount, Long gasLimit, Long gasPrice) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(CoinType.ETHEREUM, fromAddress)) {
                privateKey = walletService.getCoinsMap().get(CoinType.ETHEREUM).getPrivateKey();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.getWallet().getKey(CoinType.ETHEREUM, path);
            }

            Integer nonce = getNonce(fromAddress);

            Ethereum.SigningInput.Builder input = Ethereum.SigningInput.newBuilder();
            input.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKey.data()))));
            input.setToAddress(toAddress);
            input.setChainId(ByteString.copyFrom(Numeric.hexStringToByteArray("1")));
            input.setNonce(ByteString.copyFrom(Numeric.hexStringToByteArray(Integer.toHexString(nonce))));
            input.setGasLimit(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(gasLimit))));
            input.setGasPrice(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(gasPrice))));
            input.setAmount(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(amount.multiply(ETH_DIVIDER).longValue()))));

            Ethereum.SigningOutput output = AnySigner.sign(input.build(), CoinType.ETHEREUM, Ethereum.SigningOutput.parser());

            return Numeric.toHexString(output.getEncoded().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String sign(ERC20 token, String fromAddress, String toAddress, BigDecimal amount, Long gasLimit, Long gasPrice) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(CoinType.ETHEREUM, fromAddress)) {
                privateKey = walletService.getCoinsMap().get(CoinType.ETHEREUM).getPrivateKey();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.getWallet().getKey(CoinType.ETHEREUM, path);
            }

            Integer nonce = getNonce(fromAddress);

            Ethereum.SigningInput.Builder input = Ethereum.SigningInput.newBuilder();
            input.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKey.data()))));
            input.setToAddress(token.getContractAddress());
            input.setChainId(ByteString.copyFrom(Numeric.hexStringToByteArray("1")));
            input.setNonce(ByteString.copyFrom(Numeric.hexStringToByteArray(Integer.toHexString(nonce))));
            input.setGasLimit(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(gasLimit))));
            input.setGasPrice(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(gasPrice))));

            EthereumAbiFunction function = new EthereumAbiFunction("transfer");
            byte[] amountBytes = amount.multiply(ETH_DIVIDER).toBigInteger().toByteArray();
            function.addParamAddress(Numeric.hexStringToByteArray(toAddress), false);
            function.addParamUInt256(amountBytes, false);
            byte[] encode = EthereumAbi.encode(function);

            input.setPayload(ByteString.copyFrom(encode));

            Ethereum.SigningOutput output = AnySigner.sign(input.build(), CoinType.ETHEREUM, Ethereum.SigningOutput.parser());

            return Numeric.toHexString(output.getEncoded().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addAddressToJournal(String address) {
        address = address.toLowerCase();

        mongo.getCollection(ADDRESS_COLL).findOneAndUpdate(
                new Document("address", address),
                new Document("$set", new Document("address", address).append("timestamp", System.currentTimeMillis())),
                new FindOneAndUpdateOptions().upsert(true));
    }

    public boolean existsInJournal(String fromAddress, String toAddress) {
        BasicDBList or = new BasicDBList();
        or.add(new Document("address", fromAddress));
        or.add(new Document("address", toAddress));

        return mongo.getCollection(ADDRESS_COLL).find(new Document("$or", or)).iterator().hasNext();
    }

    public Long getGasLimit(String address) {
        try {
            return web3.ethEstimateGas(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, address, null)).send().getAmountUsed().longValue();
        } catch (Exception e) {
        }

        return ethInitialGasLimit;
    }

    public Long getAvgGasPrice() {
        return Long.valueOf(cacheService.getEtherscanGasPrice().optJSONObject("result").optString("ProposeGasPrice")) * 1000_000_000;
    }

    public Long getFastGasPrice() {
        return Long.valueOf(cacheService.getEtherscanGasPrice().optJSONObject("result").optString("FastGasPrice")) * 1000_000_000;
    }

    private String convertAddress32BytesTo20Bytes(String contractAddress, String address32Bytes) {
        String address20Bytes = Numeric.prependHexPrefix(address32Bytes.substring(address32Bytes.length() - 40));

        if (Numeric.toBigInt(address20Bytes).intValue() == 0) {
            return contractAddress;
        }

        return address20Bytes;
    }

    private static TransactionListDTO buildTransactionList(String coll, Document search, String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        try {
            Map<String, TransactionDetailsDTO> map = getNodeTransactionsFromDB(coll, search, address).getMap();

            return TxUtil.buildTxs(map, startIndex, limit, txDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    private static NodeTransactionsDTO getNodeTransactionsFromDB(String coll, Document search, String address) {
        try {
            Map<String, TransactionDetailsDTO> map = new HashMap<>();

            mongo.getCollection(coll).find(search).into(new ArrayList<>()).stream().forEach(d -> {
                TransactionDetailsDTO dto = new TransactionDetailsDTO();

                String fromAddress = d.getString("fromAddress");
                String toAddress = d.getString("toAddress");

                dto.setTxId(d.getString("txId"));
                dto.setType(TransactionType.getType(fromAddress, toAddress, address));
                dto.setStatus(TransactionStatus.valueOf(d.getInteger("status")));
                dto.setCryptoAmount(d.get("amount", Decimal128.class).bigDecimalValue());
                dto.setCryptoFee(d.get("fee", Decimal128.class).bigDecimalValue());
                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setDate1(new Date(d.getLong("blockTime")));

                map.put(d.getString("txId"), dto);
            });

            return new NodeTransactionsDTO(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new NodeTransactionsDTO();
    }

    private static TransactionDetailsDTO getTransactionFromDB(String coll, Document search, String address) {
        try {
            Document txDoc = mongo.getCollection(coll).find(search).first();

            if (txDoc == null) {
                return new TransactionDetailsDTO();
            } else {
                String txId = txDoc.getString("txId");
                String fromAddress = txDoc.getString("fromAddress");
                String toAddress = txDoc.getString("toAddress");

                TransactionDetailsDTO dto = new TransactionDetailsDTO();
                dto.setTxId(txId);
                dto.setLink(explorerUrl + "/" + txId);
                dto.setType(TransactionType.getType(fromAddress, toAddress, address));
                dto.setCryptoAmount(txDoc.get("amount", Decimal128.class).bigDecimalValue());
                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setCryptoFee(txDoc.get("fee", Decimal128.class).bigDecimalValue());
                dto.setStatus(TransactionStatus.valueOf(txDoc.getInteger("status")));
                dto.setDate2(new Date(txDoc.getLong("blockTime")));

                return dto;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionDetailsDTO();
    }

    private static void addPendingTransaction(String txId, String fromAddress, String toAddress, BigDecimal amount, BigDecimal fee) {
        Document doc = new Document("txId", txId.toLowerCase())
                .append("fromAddress", fromAddress.toLowerCase())
                .append("toAddress", toAddress.toLowerCase())
                .append("amount", amount)
                .append("fee", fee)
                .append("status", TransactionStatus.PENDING.getValue())
                .append("blockTime", System.currentTimeMillis())
                .append("timestamp", System.currentTimeMillis());

        mongo.getCollection(ETH_TX_COLL).insertOne(doc);
    }

    private static void addPendingTransaction(ERC20 token, String txId, String fromAddress, String toAddress, BigDecimal amount, BigDecimal fee) {
        addPendingTransaction(txId, fromAddress, token.getContractAddress(), BigDecimal.ZERO, fee);

        Document doc = new Document("txId", txId.toLowerCase())
                .append("fromAddress", fromAddress.toLowerCase())
                .append("toAddress", toAddress.toLowerCase())
                .append("amount", amount)
                .append("fee", fee)
                .append("status", TransactionStatus.PENDING.getValue())
                .append("blockTime", System.currentTimeMillis())
                .append("timestamp", System.currentTimeMillis())
                .append("token", token.name());

        mongo.getCollection(TOKEN_TX_COLL).insertOne(doc);
    }

    private void fetchEthTransaction(org.web3j.protocol.core.methods.response.Transaction tx, Long timestamp, List<UpdateOneModel<Document>> ethTxs, List<UpdateOneModel<Document>> tokenTxs) {
        try {
            String txId = tx.getHash();
            String fromAddress = tx.getFrom();
            String toAddress = tx.getTo();

            if (existsInJournal(fromAddress, toAddress)) {
                BigDecimal amount = new BigDecimal(tx.getValue()).divide(ETH_DIVIDER).stripTrailingZeros();
                BigDecimal fee = new BigDecimal(tx.getGasPrice()).multiply(new BigDecimal(tx.getGas())).divide(ETH_DIVIDER).stripTrailingZeros();
                Integer blockNumber = parseBlockNumber(tx);

                Optional<TransactionReceipt> receiptOptional = web3.ethGetTransactionReceipt(txId).send().getTransactionReceipt();

                if (receiptOptional.isPresent()) {
                    TransactionReceipt receipt = receiptOptional.get();
                    TransactionStatus status = getTransactionStatus(receipt);

                    if (amount.compareTo(BigDecimal.ZERO) == 0) {
                        Document tokenDoc = fetchTokenTransaction(txId, blockNumber, timestamp, fee, status, receipt);

                        if (tokenDoc != null) {
                            fromAddress = Util.nvl(fromAddress, tokenDoc.getString("fromAddress"));
                            toAddress = Util.nvl(toAddress, tokenDoc.getString("toAddress"));

                            tokenDoc.put("fromAddress", fromAddress);
                            tokenDoc.put("toAddress", toAddress);

                            UpdateOneModel tokenUpdate = new UpdateOneModel(new Document("txId", tokenDoc.getString("txId")), new Document("$set", tokenDoc));
                            tokenUpdate.getOptions().upsert(true);

                            tokenTxs.add(tokenUpdate);
                        }
                    }

                    Document doc = new Document("txId", txId)
                            .append("blockNumber", blockNumber)
                            .append("fromAddress", fromAddress)
                            .append("toAddress", toAddress)
                            .append("status", status.getValue())
                            .append("amount", amount)
                            .append("fee", fee)
                            .append("blockTime", timestamp)
                            .append("timestamp", System.currentTimeMillis());

                    UpdateOneModel update = new UpdateOneModel(new Document("txId", doc.getString("txId")), new Document("$set", doc));
                    update.getOptions().upsert(true);

                    ethTxs.add(update);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document fetchTokenTransaction(String txId, Integer blockNumber, Long blockTime, BigDecimal fee, TransactionStatus status, TransactionReceipt receipt) {
        try {
            if (receipt.getLogs().size() > 0) {
                Log log = receipt.getLogs().get(0);
                BigDecimal amountToken = parseTokenAmount(log.getData());

                if (catmContractAddress.equalsIgnoreCase(log.getAddress())) {
                    String fromAddressToken = convertAddress32BytesTo20Bytes(catmContractAddress, log.getTopics().get(1));
                    String toAddressToken = convertAddress32BytesTo20Bytes(catmContractAddress, log.getTopics().get(2));

                    return new Document("txId", txId)
                            .append("blockNumber", blockNumber)
                            .append("fromAddress", fromAddressToken)
                            .append("toAddress", toAddressToken)
                            .append("amount", amountToken)
                            .append("fee", fee)
                            .append("status", status.getValue())
                            .append("blockTime", blockTime)
                            .append("timestamp", System.currentTimeMillis())
                            .append("token", ERC20.CATM.name());
                }

                if (usdtContractAddress.equalsIgnoreCase(log.getAddress())) {
                    String fromAddressToken = convertAddress32BytesTo20Bytes(catmContractAddress, log.getTopics().get(1));
                    String toAddressToken = convertAddress32BytesTo20Bytes(catmContractAddress, log.getTopics().get(2));

                    return new Document("txId", txId)
                            .append("blockNumber", blockNumber)
                            .append("fromAddress", fromAddressToken)
                            .append("toAddress", toAddressToken)
                            .append("amount", amountToken)
                            .append("fee", fee)
                            .append("status", status.getValue())
                            .append("blockTime", blockTime)
                            .append("timestamp", System.currentTimeMillis())
                            .append("token", ERC20.USDT.name());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Document("txId", txId)
                .append("blockNumber", blockNumber)
                .append("fee", fee)
                .append("status", status.getValue())
                .append("blockTime", blockTime)
                .append("timestamp", System.currentTimeMillis())
                .append("token", ERC20.CATM.name());
    }

    private BigDecimal parseTokenAmount(String data) {
        if (Numeric.cleanHexPrefix(data).isEmpty()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(Numeric.toBigInt(data)).divide(ETH_DIVIDER).stripTrailingZeros();
    }

    private Integer parseBlockNumber(org.web3j.protocol.core.methods.response.Transaction tx) {
        try {
            return tx.getBlockNumber().intValue();
        } catch (Exception e) {
        }

        return 0;
    }

    private BulkWriteResult bulkWrite(String coll, List<UpdateOneModel<Document>> updates) {
        if (!updates.isEmpty()) {
            return mongo.getCollection(coll).bulkWrite(updates);
        }

        return null;
    }

    private org.web3j.protocol.core.methods.response.Transaction getTransactionByHash(String txId) {
        try {
            return web3.ethGetTransactionByHash(txId).send().getTransaction().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}