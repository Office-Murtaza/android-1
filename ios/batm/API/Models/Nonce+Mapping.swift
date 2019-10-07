import ObjectMapper

extension Nonce: ImmutableMappable {
  init(map: Map) throws {
    nonce = try map.value("nonce")
  }
}

