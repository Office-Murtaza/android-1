package com.belco.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.core.jni.CoinType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NodeService {

    private final Map<CoinType, String> mainNodeMap = new ConcurrentHashMap<>();
    private final Map<CoinType, String> reserveNodeMap = new ConcurrentHashMap<>();
    private final Map<CoinType, String> explorerMap = new ConcurrentHashMap<>();

    public NodeService(
            @Value("${btc.node.main.url}") String btcNodeMainUrl,
            @Value("${btc.node.reserve.url}") String btcNodeReserveUrl,
            @Value("${ltc.node.main.url}") String ltcNodeMainUrl,
            @Value("${bch.node.main.url}") String bchNodeMainUrl,
            @Value("${dash.node.main.url}") String dashNodeMainUrl,
            @Value("${doge.node.main.url}") String dogeNodeMainUrl,
            @Value("${eth.node.main.url}") String ethNodeMainUrl,

            //@Value("${eth.node.reserve.url}") String ethNodeReserveUrl,

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

        mainNodeMap.put(CoinType.BITCOIN, btcNodeMainUrl);
        mainNodeMap.put(CoinType.LITECOIN, ltcNodeMainUrl);
        mainNodeMap.put(CoinType.BITCOINCASH, bchNodeMainUrl);
        mainNodeMap.put(CoinType.DASH, dashNodeMainUrl);
        mainNodeMap.put(CoinType.DOGECOIN, dogeNodeMainUrl);
        mainNodeMap.put(CoinType.ETHEREUM, ethNodeMainUrl);
        mainNodeMap.put(CoinType.BINANCE, bnbNodeMainUrl);
        mainNodeMap.put(CoinType.XRP, xrpNodeMainUrl);
        mainNodeMap.put(CoinType.TRON, trxNodeMainUrl);

        reserveNodeMap.put(CoinType.BITCOIN, btcNodeReserveUrl);
        reserveNodeMap.put(CoinType.XRP, xrpNodeReserveUrl);

        explorerMap.put(CoinType.BITCOIN, btcExplorerUrl);
        explorerMap.put(CoinType.LITECOIN, ltcExplorerUrl);
        explorerMap.put(CoinType.BITCOINCASH, bchExplorerUrl);
        explorerMap.put(CoinType.DASH, dashExplorerUrl);
        explorerMap.put(CoinType.DOGECOIN, dogeExplorerUrl);
        explorerMap.put(CoinType.ETHEREUM, ethExplorerUrl);
        explorerMap.put(CoinType.BINANCE, bnbExplorerUrl);
        explorerMap.put(CoinType.XRP, xrpExplorerUrl);
        explorerMap.put(CoinType.TRON, trxExplorerUrl);
    }

    public String getNodeUrl(CoinType coinType) {
        return mainNodeMap.get(coinType);
    }

    public String getExplorerUrl(CoinType coinType) {
        return explorerMap.get(coinType);
    }

    public boolean isNodeAvailable(CoinType coinType) {
        return mainNodeMap.containsKey(coinType);
    }

    public boolean switchToReserveNode(CoinType coinType) {
        if (reserveNodeMap.containsKey(coinType)) {
            mainNodeMap.put(coinType, reserveNodeMap.get(coinType));
            reserveNodeMap.remove(coinType);

            return true;
        }

        return false;
    }
}