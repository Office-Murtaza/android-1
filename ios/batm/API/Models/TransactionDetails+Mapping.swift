import ObjectMapper

extension TransactionDetails: ImmutableMappable {
  init(map: Map) throws {
    txid = try map.value("txId")
    link = try map.value("link")
    type = TransactionType(rawValue: try map.value("type"))
    status = TransactionStatus(rawValue: try map.value("status"))
    amount = try map.value("cryptoAmount")
    fee = try map.value("cryptoFee")
    dateString = try map.value("date2")
    fromAddress = try map.value("fromAddress")
    toAddress = try map.value("toAddress")
    phone = try map.value("phone")
    imageId = try map.value("imageId")
    message = try map.value("message")
    sellInfo = try map.value("sellInfo")
  }
}
