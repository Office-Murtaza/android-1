import ObjectMapper

extension SellDetails: ImmutableMappable {
  init(map: Map) throws {
    dailyLimit = try map.value("dailyLimit.USD", using: DecimalDoubleTransform())
    transactionLimit = try map.value("txLimit.USD", using: DecimalDoubleTransform())
    profitRate = try map.value("sellProfitRate.USD", using: DecimalDoubleTransform())
  }
}

