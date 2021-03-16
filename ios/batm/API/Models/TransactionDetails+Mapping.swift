import ObjectMapper
import TrustWalletCore

extension TransactionDetails: ImmutableMappable {
    init(map: Map) throws {
        txId = try? map.value("txId")
        txDbId = try? map.value("txDbId")
        link = try? map.value("link")
        type = TransactionType(rawValue: try map.value("type"))
        status = TransactionStatus(rawValue: try map.value("status"))
        confirmations = try? map.value("confirmations")
        cryptoAmount = try? map.value("cryptoAmount")
        cryptoFee = try? map.value("cryptoFee")
        fromAddress = try? map.value("fromAddress")
        toAddress = try? map.value("toAddress")
        fromPhone = try? map.value("fromPhone")
        toPhone = try? map.value("toPhone")
        imageId = try? map.value("imageId")
        message = try? map.value("message")
        swapTxId = try? map.value("swapTxId")
        swapLink = try? map.value("swapLink")
        let coinCode: String = (try? map.value("swapCoin")) ?? ""
        swapCoin = CustomCoinType(code: coinCode)
        swapCryptoAmount = try? map.value("swapCryptoAmount")
        fiatAmount = try? map.value("fiatAmount")
        cashStatus = try? map.value("cashStatus")
        sellInfo = try? map.value("sellInfo")
        let timestamp: Int = try map.value("timestamp")
        date = timestamp.timestampToStringDate(format: GlobalConstants.longDateForm)
    }
}
