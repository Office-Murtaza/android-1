import ObjectMapper

extension Transaction: ImmutableMappable {
  init(map: Map) throws {
    txId = try map.value("txId")
    txDbId = try map.value("txDbId")
    dateString = try map.value("date1")
    type = TransactionType(rawValue: try map.value("type"))
    status = TransactionStatus(rawValue: try map.value("status"))
    amount = try map.value("cryptoAmount")
  }
}
