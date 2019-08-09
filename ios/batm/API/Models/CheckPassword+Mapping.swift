import ObjectMapper

extension CheckPassword: ImmutableMappable {
  init(map: Map) throws {
    matched = try map.value("match")
  }
}
