import ObjectMapper

extension ResponseResult: ImmutableMappable {
  init(map: Map) throws {
    result = try map.value("result")
  }
}
