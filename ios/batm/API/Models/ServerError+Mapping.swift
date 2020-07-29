import ObjectMapper

extension ServerError: ImmutableMappable {
  init(map: Map) throws {
    code = try map.value("code")
    message = try map.value("message")
  }
}
