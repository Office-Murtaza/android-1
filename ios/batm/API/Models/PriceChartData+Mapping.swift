import ObjectMapper

extension PriceChartData: ImmutableMappable {
  init(map: Map) throws {
    price = try map.value("price", using: DecimalDoubleTransform())
    balance = try map.value("balance", using: DecimalDoubleTransform())
    periods = try map.value("chart")
  }
}

