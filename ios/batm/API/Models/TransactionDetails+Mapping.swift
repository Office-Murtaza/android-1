import ObjectMapper
import TrustWalletCore

extension TransactionDetails: ImmutableMappable {
    init(map: Map) throws {
        txId = try? map.value("txId")
        txDbId = try? map.value("txDbId")
        link = try? map.value("link")
        let coinCode: String = (try? map.value("coin")) ?? ""
        coin = CustomCoinType(code: coinCode)
        userId = try? map.value("userId")
        type = TransactionType(rawValue: (try? map.value("type")) ?? 0)
        status = TransactionStatus(rawValue: (try? map.value("status")) ?? 0)
        confirmations = try? map.value("confirmations")
        cryptoAmount = try? map.value("cryptoAmount")
        cryptoFee = try? map.value("cryptoFee")
        fromAddress = try? map.value("fromAddress")
        toAddress = try? map.value("toAddress")
        fromPhone = try? map.value("fromPhone")
        toPhone = try? map.value("toPhone")
        image = try? map.value("image")
        message = try? map.value("message")
        refTxId = try? map.value("refTxId")
        refLink = try? map.value("refLink")
        let refCoinCode: String = (try? map.value("refCoin")) ?? ""
        refCoin = CustomCoinType(code: refCoinCode)
        refCryptoAmount = try? map.value("refCryptoAmount")
        fiatAmount = try? map.value("fiatAmount")
        cashStatus = try? map.value("cashStatus")
        sellInfo = try? map.value("sellInfo")
        processed = try? map.value("processed")
        timestamp = try map.value("timestamp")
    }
}
