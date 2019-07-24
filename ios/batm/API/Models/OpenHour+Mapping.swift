import ObjectMapper

extension OpenHour: ImmutableMappable {
  init(map: Map) throws {
    days = try map.value("days")
    hours = try map.value("hours")
  }
}
