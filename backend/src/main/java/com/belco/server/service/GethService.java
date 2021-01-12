package com.belco.server.service;

import com.belco.server.dto.SubmitTransactionDTO;
import com.belco.server.dto.TransactionDetailsDTO;
import com.belco.server.dto.TransactionHistoryDTO;
import com.belco.server.entity.TransactionRecord;
import com.belco.server.entity.TransactionRecordWallet;
import com.belco.server.model.TransactionStatus;
import com.belco.server.model.TransactionType;
import com.belco.server.token.CATM;
import com.belco.server.token.USDT;
import com.belco.server.util.TxUtil;
import com.belco.server.util.Util;
import com.google.protobuf.ByteString;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOneModel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.Decimal128;
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
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
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
    public static final BigDecimal USDT_DIVIDER = BigDecimal.valueOf(1_000_000L);

    private static final CoinType ETHEREUM = CoinType.ETHEREUM;

    private static final int START_BLOCK = 10290000;
    private static final int MAX_BLOCK_COUNT = 500;
    private static final long WATCH_TIME = 1800000;

    private static final String ADDRESS_COLL = "eth_address";
    private static final String BLOCK_COLL = "eth_block";
    private static final String ETH_TX_COLL = "eth_transaction";
    private static final String TOKEN_TX_COLL = "token_transaction";

    public static Web3j web3;
    public static CATM catm;
    public static USDT usdt;
    private static long ethInitialGasLimit;
    private static String catmContractAddress;
    private static String usdtContractAddress;
    private static RestTemplate rest;
    private static MongoTemplate mongo;
    private static CacheService cacheService;
    private static WalletService walletService;
    private static NodeService nodeService;

    public GethService(@Value("${eth.initial.gas-limit}") long ethInitialGasLimit,
                       @Value("${catm.contract.address}") String catmContractAddress,
                       @Value("${usdt.contract.address}") String usdtContractAddress,
                       RestTemplate rest,
                       MongoTemplate mongo,
                       CacheService cacheService,
                       WalletService walletService,
                       NodeService nodeService) {

        GethService.ethInitialGasLimit = ethInitialGasLimit;
        GethService.catmContractAddress = catmContractAddress;
        GethService.usdtContractAddress = usdtContractAddress;
        GethService.rest = rest;
        GethService.mongo = mongo;
        GethService.cacheService = cacheService;
        GethService.walletService = walletService;
        GethService.nodeService = nodeService;

        init();
    }

    public static String submitTransaction(ERC20 token, SubmitTransactionDTO dto) {
        if (nodeService.isNodeAvailable(ETHEREUM)) {
            try {
                String txId = web3.ethSendRawTransaction(dto.getHex()).send().getTransactionHash();

                if (StringUtils.isNotBlank(txId)) {
                    addPendingTransaction(token, txId, dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());

                    return txId;
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(ETHEREUM)) {
                    init();
                    return submitTransaction(token, dto);
                }
            }
        }

        return null;
    }

    public static Long getAvgGasPrice() {
        return Long.valueOf(cacheService.getEtherscanGasPrice().optJSONObject("result").optString("ProposeGasPrice")) * 1000_000_000;
    }

    public static Long getFastGasPrice() {
        return Long.valueOf(cacheService.getEtherscanGasPrice().optJSONObject("result").optString("FastGasPrice")) * 1000_000_000;
    }

    private static void init() {
        try {
            web3 = Web3j.build(new HttpService(nodeService.getNodeUrl(ETHEREUM)));

            ContractGasProvider gasProvider = new StaticGasProvider(BigInteger.valueOf(getFastGasPrice()), BigInteger.valueOf(ethInitialGasLimit));

            catm = CATM.load(catmContractAddress, web3,
                    Credentials.create(Numeric.toHexString(walletService.getCoinsMap().get(CoinType.ETHEREUM).getPrivateKey().data())), gasProvider);

            usdt = USDT.load(usdtContractAddress, web3,
                    Credentials.create(Numeric.toHexString(walletService.getCoinsMap().get(CoinType.ETHEREUM).getPrivateKey().data())), gasProvider);
        } catch (Exception e) {
            if (nodeService.switchToReserveNode(ETHEREUM)) {
                init();
            }
        }
    }

    private static TransactionHistoryDTO buildTransactionList(String coll, BasicDBObject query, String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionRecordWallet> transactionRecordWallets) {
        return TxUtil.buildTxs(getNodeTransactionsFromDB(coll, query, address), startIndex, limit, transactionRecords, transactionRecordWallets);
    }

    private static Map<String, TransactionDetailsDTO> getNodeTransactionsFromDB(String coll, BasicDBObject query, String address) {
        Map<String, TransactionDetailsDTO> map = new HashMap<>();

        mongo.getCollection(coll).find(query).into(new ArrayList<>()).stream().forEach(d -> {
            try {
                TransactionDetailsDTO dto = new TransactionDetailsDTO();

                String fromAddress = d.getString("fromAddress");
                String toAddress = d.getString("toAddress");

                dto.setTxId(d.getString("txId"));
                dto.setType(TransactionType.getType(fromAddress, toAddress, address));
                dto.setStatus(TransactionStatus.valueOf(d.getInteger("status")));
                dto.setCryptoAmount(d.get("amount", Decimal128.class).bigDecimalValue());

                try {
                    dto.setCryptoFee(d.get("fee", Decimal128.class).bigDecimalValue());
                } catch (Exception e) {
                    System.out.println(" !!!! fee: " + d.getString("fee"));
                    dto.setCryptoFee(BigDecimal.ZERO);
                }

                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setDate1(new Date(d.getLong("blockTime")));

                map.put(d.getString("txId"), dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return map;
    }

    private static TransactionDetailsDTO getTransactionFromDB(String coll, BasicDBObject query, String address, String explorerUrl) {
        try {
            Document txDoc = mongo.getCollection(coll).find(query).first();

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

    private static BasicDBObject buildQuery(String address) {
        BasicDBList or = new BasicDBList();
        or.add(new BasicDBObject("fromAddress", address.toLowerCase()));
        or.add(new BasicDBObject("toAddress", address.toLowerCase()));

        return new BasicDBObject("$or", or);
    }

    private static BasicDBObject buildQuery(ERC20 token, String address) {
        BasicDBList and = new BasicDBList();
        and.add(buildQuery(address));
        and.add(new BasicDBObject("token", token.name()));

        return new BasicDBObject("$and", and);
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void storeNodeTransactions() {
        if (nodeService.isNodeAvailable(ETHEREUM)) {
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

                if (nodeService.switchToReserveNode(ETHEREUM)) {
                    init();
                    storeNodeTransactions();
                }
            }
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
        if (nodeService.isNodeAvailable(ETHEREUM)) {
            try {
                EthGetBalance getBalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();

                return new BigDecimal(getBalance.getBalance()).divide(ETH_DIVIDER);
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(ETHEREUM)) {
                    init();
                    return getBalance(address);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(SubmitTransactionDTO dto) {
        if (nodeService.isNodeAvailable(ETHEREUM)) {
            try {
                String txId = web3.ethSendRawTransaction(dto.getHex()).send().getTransactionHash();

                if (StringUtils.isNotBlank(txId)) {
                    addPendingTransaction(txId, dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());
                    return txId;
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(ETHEREUM)) {
                    init();
                    return submitTransaction(dto);
                }
            }
        }

        return null;
    }

    public BigDecimal getTxFee(Long gasLimit, Long gasPrice) {
        return new BigDecimal(gasLimit).multiply(new BigDecimal(gasPrice)).divide(ETH_DIVIDER).stripTrailingZeros();
    }

    public Integer getNonce(String address) {
        if (nodeService.isNodeAvailable(ETHEREUM)) {
            try {
                return web3.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send().getTransactionCount().intValue();
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(ETHEREUM)) {
                    init();
                    return getNonce(address);
                }
            }
        }

        return null;
    }

    public TransactionStatus getTransactionStatus(String txId) {
        if (nodeService.isNodeAvailable(ETHEREUM)) {
            try {
                Optional<TransactionReceipt> receiptOptional = web3.ethGetTransactionReceipt(txId).send().getTransactionReceipt();

                if (receiptOptional.isPresent()) {
                    return getTransactionStatus(receiptOptional.get());
                } else {
                    return TransactionStatus.FAIL;
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(ETHEREUM)) {
                    init();
                    return getTransactionStatus(txId);
                }
            }
        }

        return TransactionStatus.PENDING;
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

    public void addAddressToJournal(String address) {
        mongo.getCollection(ADDRESS_COLL).findOneAndUpdate(new BasicDBObject("address", address.toLowerCase()), new BasicDBObject("$set", new BasicDBObject("address", address.toLowerCase()).append("timestamp", System.currentTimeMillis())), new FindOneAndUpdateOptions().upsert(true));
    }

    public boolean isAddressInJournal(String... addresses) {
        if (addresses.length == 0) {
            return false;
        }

        BasicDBList or = new BasicDBList();
        Arrays.stream(addresses).filter(e -> StringUtils.isNotBlank(e)).forEach(e -> or.add(new BasicDBObject("address", e.toLowerCase())));

        return mongo.getCollection(ADDRESS_COLL).find(new BasicDBObject("$or", or)).iterator().hasNext();
    }

    public TransactionDetailsDTO getTransactionDetails(String txId, String address, String explorerUrl) {
        return getTransactionFromDB(ETH_TX_COLL, new BasicDBObject("txId", txId.toLowerCase()), address, explorerUrl);
    }

    public TransactionDetailsDTO getTransactionDetails(ERC20 token, String txId, String address, String explorerUrl) {
        return getTransactionFromDB(TOKEN_TX_COLL, new BasicDBObject("txId", txId.toLowerCase()).append("token", token.name()), address, explorerUrl);
    }

    public TransactionHistoryDTO getTransactionHistory(String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionRecordWallet> transactionRecordWallets) {
        BasicDBObject query = buildQuery(address);

        return buildTransactionList(ETH_TX_COLL, query, address, startIndex, limit, transactionRecords, transactionRecordWallets);
    }

    public TransactionHistoryDTO getTransactionHistory(ERC20 token, String address, Integer startIndex, Integer limit, List<TransactionRecord> transactionRecords, List<TransactionRecordWallet> transactionRecordWallets) {
        BasicDBObject query = buildQuery(token, address);

        return buildTransactionList(TOKEN_TX_COLL, query, address, startIndex, limit, transactionRecords, transactionRecordWallets);
    }

    public Map<String, TransactionDetailsDTO> getNodeTransactions(String address) {
        BasicDBObject query = buildQuery(address);

        return getNodeTransactionsFromDB(ETH_TX_COLL, query, address);
    }

    public Map<String, TransactionDetailsDTO> getNodeTransactions(ERC20 token, String address) {
        BasicDBObject query = buildQuery(token, address);

        return getNodeTransactionsFromDB(ETH_TX_COLL, query, address);
    }

    public String sign(String fromAddress, String toAddress, BigDecimal amount, Long gasLimit, Long gasPrice) {
        try {
            PrivateKey privateKey = getPrivateKey(fromAddress);
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

    private PrivateKey getPrivateKey(String fromAddress) {
        if (walletService.isServerAddress(CoinType.ETHEREUM, fromAddress)) {
            return walletService.getCoinsMap().get(CoinType.ETHEREUM).getPrivateKey();
        } else {
            String path = walletService.getPath(fromAddress);
            return walletService.getWallet().getKey(CoinType.ETHEREUM, path);
        }
    }

    public String sign(ERC20 token, String fromAddress, String toAddress, BigDecimal amount, Long gasLimit, Long gasPrice) {
        try {
            PrivateKey privateKey = getPrivateKey(fromAddress);
            Integer nonce = getNonce(fromAddress);

            Ethereum.SigningInput.Builder input = Ethereum.SigningInput.newBuilder();
            input.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKey.data()))));
            input.setToAddress(token.getContractAddress());
            input.setChainId(ByteString.copyFrom(Numeric.hexStringToByteArray("1")));
            input.setNonce(ByteString.copyFrom(Numeric.hexStringToByteArray(Integer.toHexString(nonce))));
            input.setGasLimit(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(gasLimit))));
            input.setGasPrice(ByteString.copyFrom(Numeric.hexStringToByteArray(Long.toHexString(gasPrice))));

            EthereumAbiFunction function = new EthereumAbiFunction("transfer");
            byte[] amountBytes = amount.multiply(token.getDivider()).toBigInteger().toByteArray();
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

    public Long getGasLimit(String address) {
        if (nodeService.isNodeAvailable(ETHEREUM)) {
            try {
                return web3.ethEstimateGas(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, address, null)).send().getAmountUsed().longValue();
            } catch (Exception e) {
            }
        }

        return ethInitialGasLimit;
    }

    private String convertAddress32BytesTo20Bytes(String contractAddress, String address32Bytes) {
        String address20Bytes = Numeric.prependHexPrefix(address32Bytes.substring(address32Bytes.length() - 40));

        if (Numeric.toBigInt(address20Bytes).intValue() == 0) return contractAddress;

        return address20Bytes;
    }

    private void fetchEthTransaction(org.web3j.protocol.core.methods.response.Transaction tx, Long timestamp, List<UpdateOneModel<Document>> ethTxs, List<UpdateOneModel<Document>> tokenTxs) {
        if (nodeService.isNodeAvailable(ETHEREUM)) {
            try {
                String txId = tx.getHash();
                String fromAddress = tx.getFrom();
                String toAddress = tx.getTo();

                if (isAddressInJournal(fromAddress, toAddress)) {
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
        if (nodeService.isNodeAvailable(ETHEREUM)) {
            try {
                return web3.ethGetTransactionByHash(txId).send().getTransaction().get();
            } catch (Exception e) {
                e.printStackTrace();

                if (nodeService.switchToReserveNode(ETHEREUM)) {
                    init();
                    return getTransactionByHash(txId);
                }
            }
        }

        return null;
    }

    public enum ERC20 {
        CATM {
            @Override
            public String getContractAddress() {
                return catmContractAddress;
            }

            @Override
            public BigDecimal getBalance(String address) {
                if (nodeService.isNodeAvailable(ETHEREUM)) {
                    try {
                        return new BigDecimal(catm.balanceOf(address).send()).divide(ETH_DIVIDER);
                    } catch (Exception e) {
                        e.printStackTrace();

                        if (nodeService.switchToReserveNode(ETHEREUM)) {
                            init();
                            return getBalance(address);
                        }
                    }
                }

                return BigDecimal.ZERO;
            }

            @Override
            public BigDecimal getDivider() {
                return ETH_DIVIDER;
            }
        },
        USDT {
            @Override
            public String getContractAddress() {
                return usdtContractAddress;
            }

            @Override
            public BigDecimal getBalance(String address) {
                if (nodeService.isNodeAvailable(ETHEREUM)) {
                    try {
                        return new BigDecimal(usdt.balanceOf(address).send()).divide(USDT_DIVIDER);
                    } catch (Exception e) {
                        e.printStackTrace();

                        if (nodeService.switchToReserveNode(ETHEREUM)) {
                            init();
                            return getBalance(address);
                        }
                    }
                }

                return BigDecimal.ZERO;
            }

            @Override
            public BigDecimal getDivider() {
                return USDT_DIVIDER;
            }
        };

        public abstract String getContractAddress();

        public abstract BigDecimal getBalance(String address);

        public abstract BigDecimal getDivider();
    }
}