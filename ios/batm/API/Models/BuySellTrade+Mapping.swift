import ObjectMapper

extension BuySellTrade: ImmutableMappable {
  init(map: Map) throws {
    id = try map.value("id")
    index = try map.value("index")
    username = try map.value("username")
    tradeCount = try map.value("tradeCount")
    tradeRate = try map.value("tradeRate")
    distance = try map.value("distance")
    paymentMethod = try map.value("paymentMethod")
    price = try map.value("price")
    minLimit = try map.value("minLimit")
    maxLimit = try map.value("maxLimit")
    terms = try map.value("terms")
  }
}

