import ObjectMapper

extension PreSubmitResponse: ImmutableMappable {
  init(map: Map) throws {
    amount = try map.value("cryptoAmount")
    address = try map.value("address")
  }
}

