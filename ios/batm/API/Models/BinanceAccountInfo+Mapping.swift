import ObjectMapper

extension BinanceAccountInfo: ImmutableMappable {
  init(map: Map) throws {
    chainId = try map.value("chainId")
    accountNumber = try map.value("accountNumber")
    sequence = try map.value("sequence")
  }
}

