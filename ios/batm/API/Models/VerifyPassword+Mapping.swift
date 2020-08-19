import ObjectMapper

extension VerifyPassword: ImmutableMappable {
  init(map: Map) throws {
    result = try map.value("result")
  }
}
