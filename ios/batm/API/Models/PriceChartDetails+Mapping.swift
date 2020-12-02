import ObjectMapper

extension PriceChartDetails: ImmutableMappable {
  init(map: Map) throws {
    prices = try map.value("prices")
  }
}
