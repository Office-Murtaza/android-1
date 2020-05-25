import ObjectMapper

enum BuySellTradesError: Error {
  case mappingError
}

extension BuySellTrades: ImmutableMappable {
  init(map: Map) throws {
    if let buyTotal: Int = try? map.value("buyTotal"), let buyTrades: [BuySellTrade] = try? map.value("buyTrades") {
      total = buyTotal
      trades = buyTrades
      return
    }
    
    if let sellTotal: Int = try? map.value("sellTotal"), let sellTrades: [BuySellTrade] = try? map.value("sellTrades") {
      total = sellTotal
      trades = sellTrades
      return
    }
    
    throw BuySellTradesError.mappingError
  }
}
