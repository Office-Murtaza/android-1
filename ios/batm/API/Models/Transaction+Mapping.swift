import ObjectMapper

extension Transaction: ImmutableMappable {
  init(map: Map) throws {
    txId = try map.value("txId")
    txDbId = try map.value("txDbId")
    let timestamp: Int? = try map.value("timestamp")
    dateString = timestamp?.toDate(format: GlobalConstants.shortDateForm)
    type = TransactionType(rawValue: try map.value("type"))
    status = TransactionStatus(rawValue: try map.value("status"))
    amount = try map.value("cryptoAmount", using: DecimalDoubleTransform())
  }
}
