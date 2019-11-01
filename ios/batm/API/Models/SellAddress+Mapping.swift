import ObjectMapper

extension SellAddress: ImmutableMappable {
  init(map: Map) throws {
    address = try map.value("address")
  }
}

