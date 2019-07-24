import ObjectMapper

extension MapAddresses: ImmutableMappable {
  init(map: Map) throws {
    addresses = try map.value("addressList")
  }
}
