import ObjectMapper

extension PreSubmitResponse: ImmutableMappable {
  init(map: Map) throws {
    amount = try map.value("cryptoAmount", using: DecimalDoubleTransform())
    address = try map.value("address")
  }
}

