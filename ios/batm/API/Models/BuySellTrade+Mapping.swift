import ObjectMapper

extension BuySellTrade: ImmutableMappable {
  init(map: Map) throws {
    id = try map.value("id")
    type = TradeType(rawValue: try map.value("type"))
    username = try map.value("username")
    paymentMethod = try map.value("paymentMethod")
    price = try map.value("price")
    distance = try map.value("distance")
    tradeCount = try map.value("tradeCount")
    tradeRate = try map.value("tradeRate")
    minLimit = try map.value("minLimit")
    maxLimit = try map.value("maxLimit")
    terms = try map.value("terms")
  }
}

