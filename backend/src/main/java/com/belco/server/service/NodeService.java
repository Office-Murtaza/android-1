package com.belco.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.core.jni.CoinType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NodeService {

    private final Map<CoinType, Set<String>> coinNodeMap = new ConcurrentHashMap<>();
    private final Map<CoinType, String> coinCurrentNodeMap = new ConcurrentHashMap<>();
    private final Map<CoinType, String> coinExplorerMap = new ConcurrentHashMap<>();

    public NodeService(
            @Value("${btc.node.main.url}") String btcNodeMainUrl,
            @Value("${btc.node.reserve.url}") String btcNodeReserveUrl,
            @Value("${ltc.node.main.url}") String ltcNodeMainUrl,
            @Value("${bch.node.main.url}") String bchNodeMainUrl,
            @Value("${dash.node.main.url}") String dashNodeMainUrl,
            @Value("${doge.node.main.url}") String dogeNodeMainUrl,
            @Value("${eth.node.main.url}") String ethNodeMainUrl,
            @Value("${eth.node.reserve.url}") String ethNodeReserveUrl,
            @Value("${bnb.node.main.url}") String bnbNodeMainUrl,
            @Value("${xrp.node.main.url}") String xrpNodeMainUrl,
            @Value("${xrp.node.reserve.url}") String xrpNodeReserveUrl,
            @Value("${trx.node.main.url}") String trxNodeMainUrl,

            @Value("${btc.explorer.url}") String btcExplorerUrl,
            @Value("${ltc.explorer.url}") String ltcExplorerUrl,
            @Value("${bch.explorer.url}") String bchExplorerUrl,
            @Value("${dash.explorer.url}") String dashExplorerUrl,
            @Value("${doge.explorer.url}") String dogeExplorerUrl,
            @Value("${eth.explorer.url}") String ethExplorerUrl,
            @Value("${bnb.explorer.url}") String bnbExplorerUrl,
            @Value("${xrp.explorer.url}") String xrpExplorerUrl,
            @Value("${trx.explorer.url}") String trxExplorerUrl) {

        coinNodeMap.put(CoinType.BITCOIN, new HashSet<>(Arrays.asList(btcNodeMainUrl, btcNodeReserveUrl)));
        coinNodeMap.put(CoinType.LITECOIN, new HashSet<>(Arrays.asList(ltcNodeMainUrl)));
        coinNodeMap.put(CoinType.BITCOINCASH, new LinkedHashSet<>(Arrays.asList(bchNodeMainUrl)));
        coinNodeMap.put(CoinType.DASH, new HashSet<>(Arrays.asList(dashNodeMainUrl)));
        coinNodeMap.put(CoinType.DOGECOIN, new HashSet<>(Arrays.asList(dogeNodeMainUrl)));
        coinNodeMap.put(CoinType.ETHEREUM, new LinkedHashSet<>(Arrays.asList(ethNodeMainUrl, ethNodeReserveUrl)));
        coinNodeMap.put(CoinType.BINANCE, new HashSet<>(Arrays.asList(bnbNodeMainUrl)));
        coinNodeMap.put(CoinType.XRP, new LinkedHashSet<>(Arrays.asList(xrpNodeMainUrl, xrpNodeReserveUrl)));
        coinNodeMap.put(CoinType.TRON, new HashSet<>(Arrays.asList(trxNodeMainUrl)));

        coinExplorerMap.put(CoinType.BITCOIN, btcExplorerUrl);
        coinExplorerMap.put(CoinType.LITECOIN, ltcExplorerUrl);
        coinExplorerMap.put(CoinType.BITCOINCASH, bchExplorerUrl);
        coinExplorerMap.put(CoinType.DASH, dashExplorerUrl);
        coinExplorerMap.put(CoinType.DOGECOIN, dogeExplorerUrl);
        coinExplorerMap.put(CoinType.ETHEREUM, ethExplorerUrl);
        coinExplorerMap.put(CoinType.BINANCE, bnbExplorerUrl);
        coinExplorerMap.put(CoinType.XRP, xrpExplorerUrl);
        coinExplorerMap.put(CoinType.TRON, trxExplorerUrl);

        coinNodeMap.keySet().stream().forEach(e -> init(e));
    }

    public String getNodeUrl(CoinType coinType) {
        return coinCurrentNodeMap.get(coinType);
    }

    public String getExplorerUrl(CoinType coinType) {
        return coinExplorerMap.get(coinType);
    }

    public boolean isNodeAvailable(CoinType coinType) {
        return coinCurrentNodeMap.containsKey(coinType);
    }

    public boolean switchToReserveNode(CoinType coinType) {
        return init(coinType);
    }

    private boolean init(CoinType coinType) {
        if (coinCurrentNodeMap.containsKey(coinType)) {
            String currentNode = coinCurrentNodeMap.get(coinType);
            Optional<String> reserveNodeOpt = coinNodeMap.get(coinType).stream().filter(e -> !e.equalsIgnoreCase(currentNode)).findFirst();

            if (reserveNodeOpt.isPresent()) {
                String reserveNode = reserveNodeOpt.get();
                coinCurrentNodeMap.put(coinType, reserveNode);

                return true;
            }
        } else {
            String currentNode = coinNodeMap.get(coinType).stream().iterator().next();
            coinCurrentNodeMap.put(coinType, currentNode);

            return true;
        }

        if (!coinCurrentNodeMap.containsKey(coinType))
            throw new RuntimeException("Nodes for " + coinType.name() + " are down");

        return false;
    }
}