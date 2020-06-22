import ObjectMapper
import TrustWalletCore

extension TransactionDetails: ImmutableMappable {
  init(map: Map) throws {
    txId = try map.value("txId")
    txDbId = try map.value("txDbId")
    link = try map.value("link")
    type = TransactionType(rawValue: try map.value("type"))
    status = TransactionStatus(rawValue: try map.value("status"))
    fiatAmount = try map.value("fiatAmount")
    cryptoAmount = try map.value("cryptoAmount")
    cryptoFee = try map.value("cryptoFee")
    dateString = try map.value("date2")
    fromAddress = try map.value("fromAddress")
    toAddress = try map.value("toAddress")
    phone = try map.value("phone")
    imageId = try map.value("imageId")
    message = try map.value("message")
    refTxId = try map.value("refTxId")
    refLink = try map.value("refLink")
    refCryptoAmount = try map.value("refCryptoAmount")
    sellInfo = try map.value("sellInfo")
    
    if let cashStatusRawValue: Int = try? map.value("cashStatus") {
      cashStatus = TransactionCashStatus(rawValue: cashStatusRawValue)
    } else {
      cashStatus = nil
    }
    
    if let code: String = try? map.value("refCoin"), let mappedRefCoin = CustomCoinType(code: code) {
      refCoin = mappedRefCoin
    } else {
      refCoin = nil
    }
  }
}
