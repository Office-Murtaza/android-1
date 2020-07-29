import ObjectMapper

extension PhoneVerificationResponse: ImmutableMappable {
  init(map: Map) throws {
    code = try map.value("code")
  }
}
