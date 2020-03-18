import ObjectMapper

extension PriceChartData: ImmutableMappable {
  init(map: Map) throws {
    price = try map.value("price")
    balance = try map.value("balance")
    periods = try map.value("chart")
  }
}

