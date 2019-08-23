import ObjectMapper

extension Transaction: ImmutableMappable {
  init(map: Map) throws {
    dateString = try map.value("date")
    type = TransactionType(rawValue: try map.value("type"))
    status = TransactionStatus(rawValue: try map.value("status"))
    amount = try map.value("value")
  }
}
