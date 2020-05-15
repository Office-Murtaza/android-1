package com.batm.service;

import com.batm.dto.NodeTransactionsDTO;
import com.batm.dto.TransactionDetailsDTO;
import com.batm.dto.TransactionListDTO;
import com.batm.dto.TxListDTO;
import com.batm.entity.Coin;
import com.batm.model.TransactionStatus;
import com.batm.model.TransactionType;
import com.batm.repository.CoinPathRep;
import com.batm.repository.CoinRep;
import com.batm.repository.UserCoinRep;
import com.batm.util.Constant;
import com.batm.util.TxUtil;
import com.google.protobuf.ByteString;
import com.mongodb.BasicDBList;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import lombok.Getter;
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
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;
import wallet.core.jni.EthereumSigner;
import wallet.core.jni.PrivateKey;
import wallet.core.jni.proto.Ethereum;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

@Getter
@Service
@EnableScheduling
public class GethService {

    private final long GAS_PRICE = 50_000_000_000L;
    private final long GAS_LIMIT = 50_000;

    private final int START_BLOCK = 10_064_660;
    private final int MAX_BLOCK_COUNT = 100;

    private final String ADDRESS_COLL = "eth_address";
    private final String BLOCK_COLL = "eth_block";
    private final String ETH_TX_COLL = "eth_transaction";
    private final String TOKEN_TX_COLL = "token_transaction";

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

    @Value("${eth.node.url}")
    private String ethNodeUrl;

    @Value("${token.node.url}")
    private String tokenNodeUrl;

    @Value("${eth.explorer.url}")
    private String ethExplorerUrl;

    @Value("${token.explorer.url}")
    private String tokenExplorerUrl;

    @Value("${eth.store.txs.enabled:false}")
    private Boolean storeEnabled;

    @Value("${token.contract.address}")
    private String contractAddress;

    private Web3j ethWeb3, tokenWeb3;

    private com.batm.contract.Token token;

    @PostConstruct
    public void init() {
        ethWeb3 = Web3j.build(new HttpService(ethNodeUrl));
        tokenWeb3 = Web3j.build(new HttpService(tokenNodeUrl));

        token = com.batm.contract.Token.load(
                contractAddress,
                tokenWeb3, Credentials.create(Numeric.toHexString(walletService.getPrivateKeyETH().data())),
                new DefaultGasProvider());

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

            Coin coin = coinRep.findCoinByCode("ETH");
            userCoinRep.findAllByCoin(coin).stream().forEach(e -> addAddressToJournal(e.getAddress()));
            coinPathRep.findAllByCoin(coin).stream().forEach(e -> addAddressToJournal(e.getAddress()));
        }
    }

    @Scheduled(cron = "0 */5 * * * *") // every 5 minutes
    public void storeEthTxs() {
        try {
            if (storeEnabled) {
                int lastSuccessBlock = mongo.getCollection(BLOCK_COLL).countDocuments() > 0 ?
                        mongo.getCollection(BLOCK_COLL).find().first().getInteger("lastSuccessBlock") : START_BLOCK;

                int lastBlockNumber = ethWeb3.ethBlockNumber().send().getBlockNumber().intValue();

                if (lastSuccessBlock < lastBlockNumber) {
                    int n = Math.min(MAX_BLOCK_COUNT, lastBlockNumber - lastSuccessBlock);
                    int toBlockNumber = lastSuccessBlock + n;

                    for (int i = lastSuccessBlock + 1; i <= toBlockNumber; i++) {
                        List<Document> ethTxs = new ArrayList<>();
                        List<Document> tokenTxs = new ArrayList<>();

                        EthBlock.Block block = ethWeb3.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true).send().getBlock();

                        block.getTransactions().stream().forEach(e -> {
                            org.web3j.protocol.core.methods.response.Transaction tx = ((EthBlock.TransactionObject) e.get()).get();

                            String txId = tx.getHash();
                            String fromAddress = tx.getFrom();
                            String toAddress = tx.getTo();

                            if (existsInJournal(fromAddress, toAddress)) {
                                BigDecimal amount = new BigDecimal(tx.getValue())
                                        .divide(Constant.ETH_DIVIDER)
                                        .stripTrailingZeros();

                                BigDecimal fee = new BigDecimal(tx.getGasPrice())
                                        .multiply(new BigDecimal(tx.getGas()))
                                        .divide(Constant.ETH_DIVIDER)
                                        .stripTrailingZeros();

                                if (amount.compareTo(BigDecimal.ZERO) == 0) {
                                    try {
                                        TransactionReceipt tr = tokenWeb3.ethGetTransactionReceipt(txId).send().getTransactionReceipt().get();
                                        Log log = tr.getLogs().get(0);

                                        if (contractAddress.equalsIgnoreCase(log.getAddress())) {
                                            BigDecimal amountToken = new BigDecimal(Numeric.toBigInt(log.getData()))
                                                    .divide(Constant.ETH_DIVIDER)
                                                    .stripTrailingZeros();

                                            String fromAddressToken = convertAddress32BytesTo20Bytes(log.getTopics().get(1));
                                            String toAddressToken = convertAddress32BytesTo20Bytes(log.getTopics().get(2));

                                            tokenTxs.add(new Document("txId", txId)
                                                    .append("blockNumber", tx.getBlockNumber().intValue())
                                                    .append("fromAddress", fromAddressToken)
                                                    .append("toAddress", toAddressToken)
                                                    .append("amount", amountToken)
                                                    .append("fee", fee)
                                                    .append("blockTime", block.getTimestamp().longValue())
                                                    .append("timestamp", System.currentTimeMillis()));
                                        }
                                    } catch (Exception f) {
                                        f.printStackTrace();
                                    }
                                }

                                ethTxs.add(new Document("txId", txId)
                                        .append("blockNumber", tx.getBlockNumber().intValue())
                                        .append("fromAddress", fromAddress)
                                        .append("toAddress", toAddress)
                                        .append("amount", amount)
                                        .append("fee", fee)
                                        .append("blockTime", block.getTimestamp().longValue())
                                        .append("timestamp", System.currentTimeMillis()));
                            }
                        });

                        if (!ethTxs.isEmpty()) {
                            mongo.getCollection(ETH_TX_COLL).deleteMany(new Document("blockNumber", i));
                            mongo.getCollection(ETH_TX_COLL).insertMany(ethTxs);
                        }

                        if (!tokenTxs.isEmpty()) {
                            mongo.getCollection(TOKEN_TX_COLL).deleteMany(new Document("blockNumber", i));
                            mongo.getCollection(TOKEN_TX_COLL).insertMany(tokenTxs);
                        }

                        mongo.getCollection(BLOCK_COLL).findOneAndUpdate(
                                new Document("lastSuccessBlock", new Document("$exists", true)),
                                new Document("$set", new Document("lastSuccessBlock", i).append("timestamp", System.currentTimeMillis())),
                                new FindOneAndUpdateOptions().upsert(true));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BigDecimal getEthBalance(String address) {
        try {
            EthGetBalance getBalance = ethWeb3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();

            return new BigDecimal(getBalance.getBalance()).divide(Constant.ETH_DIVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal getTokenBalance(String address) {
        try {
            //String res = Hash.sha3String("balanceOf(address)");
            String data = /*res.substring(2, 10)*/ "0x70a08231" + "000000000000000000000000" + Numeric.cleanHexPrefix(address);
            Transaction tr = Transaction.createEthCallTransaction(null, contractAddress, data);

            BigDecimal balanceWei = new BigDecimal(Numeric.decodeQuantity(tokenWeb3.ethCall(tr, DefaultBlockParameterName.LATEST).send().getValue()));

            return balanceWei.divide(Constant.ETH_DIVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public String submitTransaction(String hex) {
        try {
            return ethWeb3.ethSendRawTransaction(hex).send().getTransactionHash();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Long getGasPrice() {
        try {
            return ethWeb3.ethGasPrice().send().getGasPrice().longValue();
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
            return ethWeb3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send().getTransactionCount().intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public TransactionDetailsDTO getTransaction(String txId, String address) {
        try {
            Document txDoc = mongo.getCollection(ETH_TX_COLL).find(new Document("txId", txId)).first();

            if (txDoc == null) {
                return new TransactionDetailsDTO();
            } else {
                String fromAddress = txDoc.getString("fromAddress");
                String toAddress = txDoc.getString("toAddress");

                TransactionDetailsDTO dto = new TransactionDetailsDTO();
                dto.setTxId(txId);
                dto.setLink(ethExplorerUrl + "/" + txId);
                dto.setType(TransactionType.getType(fromAddress, toAddress, address));
                dto.setCryptoAmount(txDoc.get("amount", Decimal128.class).bigDecimalValue());
                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setCryptoFee(txDoc.get("fee", Decimal128.class).bigDecimalValue());
                dto.setStatus(TransactionStatus.COMPLETE);
                dto.setDate2(new Date(txDoc.getLong("blockTime") * 1000));

                return dto;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionDetailsDTO();
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

            BasicDBList or = new BasicDBList();
            or.add(new Document("fromAddress", address));
            or.add(new Document("toAddress", address));

            mongo.getCollection(ETH_TX_COLL).find(new Document("$or", or)).into(new ArrayList<>()).stream().forEach(d -> {
                TransactionDetailsDTO dto = new TransactionDetailsDTO();

                String fromAddress = d.getString("fromAddress");
                String toAddress = d.getString("toAddress");

                dto.setTxId(d.getString("txId"));
                dto.setType(TransactionType.getType(fromAddress, toAddress, address));
                dto.setCryptoAmount(d.get("amount", Decimal128.class).bigDecimalValue());
                dto.setFromAddress(fromAddress);
                dto.setToAddress(toAddress);
                dto.setCryptoFee(d.get("fee", Decimal128.class).bigDecimalValue());
                dto.setStatus(TransactionStatus.COMPLETE);
                dto.setDate1(new Date(d.getLong("blockTime") * 1000));

                map.put(d.getString("txId"), dto);
            });

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

    public void addAddressToJournal(String address) {
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

    private String convertAddress32BytesTo20Bytes(String addressBytes32) {
        String address20Bytes = Numeric.prependHexPrefix(addressBytes32.substring(addressBytes32.length() - 40));

        if (Numeric.toBigInt(address20Bytes).intValue() == 0) {
            return contractAddress;
        }

        return address20Bytes;
    }
}