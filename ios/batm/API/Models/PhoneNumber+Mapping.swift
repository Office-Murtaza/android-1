import ObjectMapper

extension PhoneNumber: ImmutableMappable {
  init(map: Map) throws {
    phoneNumber = try map.value("phone")
  }
}
