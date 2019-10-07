import ObjectMapper

extension GiftAddress: ImmutableMappable {
  init(map: Map) throws {
    address = try map.value("address")
  }
}

