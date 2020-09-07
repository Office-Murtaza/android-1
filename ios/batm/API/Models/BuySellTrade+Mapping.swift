import ObjectMapper

extension BuySellTrade: ImmutableMappable {
  init(map: Map) throws {
    id = try map.value("id")
    type = TradeType(rawValue: try map.value("type"))
    username = try map.value("trader.username")
    paymentMethod = try map.value("paymentMethod")
    price = try map.value("price", using: DecimalDoubleTransform())
    distance = try map.value("trader.distance")
    tradeCount = try map.value("trader.tradeCount")
    tradeRate = try map.value("trader.tradeRate")
    minLimit = try map.value("minLimit")
    maxLimit = try map.value("maxLimit")
    terms = try map.value("terms")
  }
}

