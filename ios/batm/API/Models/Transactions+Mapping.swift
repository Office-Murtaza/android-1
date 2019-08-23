import ObjectMapper

extension Transactions: ImmutableMappable {
  init(map: Map) throws {
    total = try map.value("total")
    transactions = try map.value("transactions")
  }
}
