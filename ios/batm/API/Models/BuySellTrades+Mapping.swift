import ObjectMapper

extension BuySellTrades: ImmutableMappable {
  init(map: Map) throws {
    total = try map.value("total")
    trades = try map.value("trades")
  }
}
