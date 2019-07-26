import ObjectMapper

extension MapAddress: ImmutableMappable {
  init(map: Map) throws {
    name = try map.value("locationName")
    address = try map.value("address")
    latitude = try map.value("latitude")
    longitude = try map.value("longitude")
    openHours = try map.value("openHours")
  }
}