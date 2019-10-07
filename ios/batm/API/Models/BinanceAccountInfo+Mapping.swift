import ObjectMapper

extension BinanceAccountInfo: ImmutableMappable {
  init(map: Map) throws {
    accountNumber = try map.value("accountNumber")
    sequence = try map.value("sequence")
  }
}

