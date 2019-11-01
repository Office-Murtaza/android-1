import ObjectMapper

extension Transaction: ImmutableMappable {
  init(map: Map) throws {
    txid = try map.value("txId")
    dateString = try map.value("date1")
    type = TransactionType(rawValue: try map.value("type"))
    status = TransactionStatus(rawValue: try map.value("status"))
    amount = try map.value("cryptoAmount")
  }
}
