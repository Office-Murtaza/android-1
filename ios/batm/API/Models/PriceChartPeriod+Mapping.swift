import ObjectMapper

extension PriceChartPeriod: ImmutableMappable {
  init(map: Map) throws {
    changeRate = try map.value("changes")
    prices = try map.value("prices")
  }
}

