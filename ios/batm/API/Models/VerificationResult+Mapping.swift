import ObjectMapper

extension VerificationResult: ImmutableMappable {
  init(map: Map) throws {
    result = try map.value("result")
  }
}
