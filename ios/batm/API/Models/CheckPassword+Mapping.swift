import ObjectMapper

extension CheckPassword: ImmutableMappable {
  init(map: Map) throws {
    result = try map.value("result")
  }
}
