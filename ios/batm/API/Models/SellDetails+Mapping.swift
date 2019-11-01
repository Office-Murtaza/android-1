import ObjectMapper

extension SellDetails: ImmutableMappable {
  init(map: Map) throws {
    dailyLimit = try map.value("dailyLimit.USD")
    transactionLimit = try map.value("txLimit.USD")
    profitRate = try map.value("sellProfitRate.USD")
  }
}

