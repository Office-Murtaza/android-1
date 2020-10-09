package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.Coin;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.repository.CoinPathRep;
import com.batm.repository.CoinRep;
import com.batm.repository.UserCoinRep;
import com.batm.util.TxUtil;
import com.batm.util.Util;
import com.google.protobuf.ByteString;
import com.mongodb.BasicDBList;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOneModel;
import lombok.Getter;
import net.sf.json.JSONObject;
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
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;
import wallet.core.java.AnySigner;
import wallet.core.jni.CoinType;
import wallet.core.jni.EthereumAbi;
import wallet.core.jni.EthereumAbiFunction;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Ethereum;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.ConnectException;
import java.util.*;

@Getter
@Service
@EnableScheduling
public class GethService {

    private static final BigDecimal ETH_DIVIDER = BigDecimal.valueOf(1_000_000_000_000_000_000L);

    private final int START_BLOCK = 10290000;
    private final int MAX_BLOCK_COUNT = 500;

    private final String ADDRESS_COLL = "eth_address";
    private final String BLOCK_COLL = "eth_block";
    private final String ETH_TX_COLL = "eth_transaction";
    private final String TOKEN_TX_COLL = "token_transaction";

    private final long WATCH_TIME = 1800000; // 30 minutes

    @Value("${eth.node.url}")
    private String nodeUrl;

    @Value("${eth.explorer.url}")
    private String explorerUrl;

    @Value("${wallet.contract.address}")
    private String contractAddress;

    @Value("${eth.gas-price.multiplier:1}")
    private double ethGasPriceMultiplier;

    @Value("${eth.gas-limit.multiplier:1}")
    private double ethGasLimitMultiplier;

    @Value("${token.gas-limit.multiplier:1}")
    private double tokenGasLimitMultiplier;

    @Autowired
    private RestTemplate rest;

    @Autowired
    private WalletService walletService;

    @Autowired
    private MongoTemplate mongo;

    @Autowired
    private CoinRep coinRep;

    @Autowired
    private UserCoinRep userCoinRep;

    @Autowired
    private CoinPathRep coinPathRep;

    private Web3j web3;

    private com.batm.contract.Token token;
    private Coin catmCoin;
    private boolean isNodeAvailable;

    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(nodeUrl)) {
            isNodeAvailable = true;

            web3 = Web3j.build(new HttpService(nodeUrl));

            token = com.batm.contract.Token.load(contractAddress, web3,
                    Credentials.create(Numeric.toHexString(walletService.getPrivateKeyETH().data())), new DefaultGasProvider());

            catmCoin = coinRep.findCoinByCode(CoinService.CoinEnum.CATM.name());

            if (!mongo.getCollection(ETH_TX_COLL).listIndexes().iterator().hasNext()) {
                mongo.getCollection(ETH_TX_COLL).createIndex(new Document("txId", 1).append("fromAddress", 1).append("toAddress", 1));
            }

            if (!mongo.getCollection(TOKEN_TX_COLL).listIndexes().iterator().hasNext()) {
                mongo.getCollection(TOKEN_TX_COLL).createIndex(new Document("txId", 1).append("fromAddress", 1).append("toAddress", 1));
            }

            if (!mongo.getCollection(ADDRESS_COLL).listIndexes().iterator().hasNext()) {
                mongo.getCollection(ADDRESS_COLL).createIndex(new Document("address", 1));
            }

            if (mongo.getCollection(ADDRESS_COLL).countDocuments() == 0) {
                addAddressToJournal(walletService.getAddressETH());
                addAddressToJournal(contractAddress);

                Coin coin = coinRep.findCoinByCode(CoinService.CoinEnum.ETH.name());
                userCoinRep.findAllByCoin(coin).stream().forEach(e -> addAddressToJournal(e.getAddress()));
                coinPathRep.findAllByCoin(coin).stream().forEach(e -> addAddressToJournal(e.getAddress()));
            }
        }
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void storeNodeTransactions() {
        if (isNodeAvailable) {
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
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
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

    public BigDecimal getEthBalance(String address) {
        if (isNodeAvailable) {
            try {
                EthGetBalance getBalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();

                return new BigDecimal(getBalance.getBalance()).divide(ETH_DIVIDER);
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal getTokenBalance(String address) {
        if (isNodeAvailable) {
            try {
                return new BigDecimal(token.balanceOf(address).send()).divide(ETH_DIVIDER);
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(SubmitTransactionDTO dto) {
        if (isNodeAvailable) {
            try {
                String txId = web3.ethSendRawTransaction(dto.getHex()).send().getTransactionHash();

                if (StringUtils.isNotBlank(txId)) {
                    addPendingEthTransaction(txId, dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());
                    return txId;
                }
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public String submitTokenTransaction(SubmitTransactionDTO dto) {
        if (isNodeAvailable) {
            try {
                String txId = web3.ethSendRawTransaction(dto.getHex()).send().getTransactionHash();

                if (StringUtils.isNotBlank(txId)) {
                    addPendingEthTransaction(txId, dto.getFromAddress(), dto.getToAddress(), BigDecimal.ZERO, dto.getFee());
                    addPendingTokenTransaction(txId, dto.getFromAddress(), dto.getToAddress(), dto.getCryptoAmount(), dto.getFee());

                    return txId;
                }
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void addPendingEthTransaction(String txId, String fromAddress, String toAddress, BigDecimal amount, BigDecimal fee) {
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

    private void addPendingTokenTransaction(String txId, String fromAddress, String toAddress, BigDecimal amount, BigDecimal fee) {
        addPendingEthTransaction(txId, fromAddress, contractAddress, BigDecimal.ZERO, fee);

        Document doc = new Document("txId", txId.toLowerCase())
                .append("fromAddress", fromAddress.toLowerCase())
                .append("toAddress", toAddress.toLowerCase())
                .append("amount", amount)
                .append("fee", fee)
                .append("status", TransactionStatus.PENDING.getValue())
                .append("blockTime", System.currentTimeMillis())
                .append("timestamp", System.currentTimeMillis());

        mongo.getCollection(TOKEN_TX_COLL).insertOne(doc);
    }

    private BigDecimal calculateFee(Long gasLimit, Long gasPrice) {
        return new BigDecimal(gasLimit).multiply(new BigDecimal(gasPrice)).divide(ETH_DIVIDER).stripTrailingZeros();
    }

    public Integer getNonce(String address) {
        if (isNodeAvailable) {
            try {
                return web3.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send().getTransactionCount().intValue();
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public TransactionStatus getTransactionStatus(String txId) {
        if (isNodeAvailable) {
            try {
                Optional<TransactionReceipt> receiptOptional = web3.ethGetTransactionReceipt(txId).send().getTransactionReceipt();

                if (receiptOptional.isPresent()) {
                    return getTransactionStatus(receiptOptional.get());
                }
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
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

    public TransactionDetailsDTO getEthTransaction(String txId, String address) {
        return getTransaction(ETH_TX_COLL, txId.toLowerCase(), address.toLowerCase());
    }

    public TransactionDetailsDTO getTokenTransaction(String txId, String address) {
        return getTransaction(TOKEN_TX_COLL, txId.toLowerCase(), address.toLowerCase());
    }

    public TransactionListDTO getEthTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        return getTransactionList(ETH_TX_COLL, address.toLowerCase(), startIndex, limit, txDTO);
    }

    public TransactionListDTO getTokenTransactionList(String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        return getTransactionList(TOKEN_TX_COLL, address.toLowerCase(), startIndex, limit, txDTO);
    }

    public NodeTransactionsDTO getEthNodeTransactions(String address) {
        return getNodeTransactions(ETH_TX_COLL, address.toLowerCase());
    }

    public NodeTransactionsDTO getTokenNodeTransactions(String address) {
        return getNodeTransactions(TOKEN_TX_COLL, address.toLowerCase());
    }

    public String ethSign(String fromAddress, String toAddress, BigDecimal amount, Long gasLimit, Long gasPrice) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(fromAddress)) {
                privateKey = walletService.getPrivateKeyETH();
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

    public String tokenSign(String fromAddress, String toAddress, BigDecimal amount, Long gasLimit, Long gasPrice) {
        try {
            PrivateKey privateKey;

            if (walletService.isServerAddress(fromAddress)) {
                privateKey = walletService.getPrivateKeyETH();
            } else {
                String path = walletService.getPath(fromAddress);
                privateKey = walletService.getWallet().getKey(CoinType.ETHEREUM, path);
            }

            Integer nonce = getNonce(fromAddress);
            Ethereum.SigningInput.Builder input = Ethereum.SigningInput.newBuilder();
            input.setPrivateKey(ByteString.copyFrom(Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(privateKey.data()))));
            input.setToAddress(contractAddress);
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

    public CoinSettingsDTO getCoinSettings(Coin coin, Long gasLimit, Long gasPrice, String walletAddress) {
        CoinSettingsDTO dto = new CoinSettingsDTO();
        dto.setProfitExchange(coin.getProfitExchange().stripTrailingZeros());
        dto.setGasLimit(gasLimit);
        dto.setGasPrice(gasPrice);
        dto.setTxFee(calculateFee(dto.getGasLimit(), dto.getGasPrice()));
        dto.setRecallFee(coin.getRecallFee());
        dto.setWalletAddress(walletAddress);
        dto.setContractAddress(contractAddress);

        return dto;
    }

    public Long getEthGasLimit(String walletAddress) {
        return getGasLimit(walletAddress, ethGasPriceMultiplier);
    }

    public Long getTokenGasLimit() {
        return getGasLimit(contractAddress, tokenGasLimitMultiplier);
    }

    public Long getGasLimit(String address, double multiplier) {
        if (isNodeAvailable) {
            try {
                return (long) (web3.ethEstimateGas(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, address, null)).send().getAmountUsed().longValue() * multiplier);
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public Long getGasPrice() {
        if (isNodeAvailable) {
            try {
                return (long) (web3.ethGasPrice().send().getGasPrice().longValue() * ethGasPriceMultiplier);
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public JSONObject getStakeDetails(String address) {
        JSONObject json = new JSONObject();
        json.put("isStakeholder", false);

        try {
            json.put("isStakeholder", token.isStakeholder(address).send().component1());
        } catch (Exception e) {
        }

        try {
            json.put("amount", token.stakeOf(address).send().intValue());
        } catch (Exception e) {
        }

        try {
            Tuple2<BigInteger, BigInteger> tuple2 = token.stakeDetails(address).send();
            json.put("startDate", tuple2.component1());
            json.put("cancelDate", tuple2.component2());
        } catch (Exception e) {
        }

        return json;
    }

    private String convertAddress32BytesTo20Bytes(String address32Bytes) {
        String address20Bytes = Numeric.prependHexPrefix(address32Bytes.substring(address32Bytes.length() - 40));

        if (Numeric.toBigInt(address20Bytes).intValue() == 0) {
            return contractAddress;
        }

        return address20Bytes;
    }

    private TransactionListDTO getTransactionList(String coll, String address, Integer startIndex, Integer limit, TxListDTO txDTO) {
        try {
            Map<String, TransactionDetailsDTO> map = getNodeTransactions(coll, address).getMap();

            return TxUtil.buildTxs(map, startIndex, limit, txDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionListDTO();
    }

    private NodeTransactionsDTO getNodeTransactions(String coll, String address) {
        try {
            Map<String, TransactionDetailsDTO> map = new HashMap<>();

            BasicDBList or = new BasicDBList();
            or.add(new Document("fromAddress", address));
            or.add(new Document("toAddress", address));

            mongo.getCollection(coll).find(new Document("$or", or)).into(new ArrayList<>()).stream().forEach(d -> {
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

    private TransactionDetailsDTO getTransaction(String coll, String txId, String address) {
        try {
            Document txDoc = mongo.getCollection(coll).find(new Document("txId", txId)).first();

            if (txDoc == null) {
                return new TransactionDetailsDTO();
            } else {
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

    private Document fetchTokenTransaction(String txId, Integer blockNumber, Long blockTime, BigDecimal fee, TransactionStatus status, TransactionReceipt receipt) {
        try {
            if (receipt.getLogs().size() > 0) {
                Log log = receipt.getLogs().get(0);

                if (contractAddress.equalsIgnoreCase(log.getAddress())) {
                    BigDecimal amountToken = parseTokenAmount(log.getData());
                    String fromAddressToken = convertAddress32BytesTo20Bytes(log.getTopics().get(1));
                    String toAddressToken = convertAddress32BytesTo20Bytes(log.getTopics().get(2));

                    return new Document("txId", txId)
                            .append("blockNumber", blockNumber)
                            .append("fromAddress", fromAddressToken)
                            .append("toAddress", toAddressToken)
                            .append("amount", amountToken)
                            .append("fee", fee)
                            .append("status", status.getValue())
                            .append("blockTime", blockTime)
                            .append("timestamp", System.currentTimeMillis());
                }
            } else {
                return new Document("txId", txId)
                        .append("blockNumber", blockNumber)
                        .append("fee", fee)
                        .append("status", status.getValue())
                        .append("blockTime", blockTime)
                        .append("timestamp", System.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void fetchEthTransaction(org.web3j.protocol.core.methods.response.Transaction tx, Long timestamp, List<UpdateOneModel<Document>> ethTxs, List<UpdateOneModel<Document>> tokenTxs) {
        if (isNodeAvailable) {
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
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        if (isNodeAvailable) {
            try {
                return web3.ethGetTransactionByHash(txId).send().getTransaction().get();
            } catch (ConnectException ce) {
                isNodeAvailable = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}