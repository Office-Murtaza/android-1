import ObjectMapper

extension RippleSequence: ImmutableMappable {
  init(map: Map) throws {
    sequence = try map.value("sequence")
  }
}

