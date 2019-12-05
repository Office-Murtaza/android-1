import ObjectMapper

extension CoinsFee: ImmutableMappable {
  init(map: Map) throws {
    fees = try map.value("fees")
  }
}

