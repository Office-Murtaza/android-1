import ObjectMapper

extension Account: ImmutableMappable {
  init(map: Map) throws {
    userId = try map.value("userId")
    accessToken = try map.value("accessToken")
    refreshToken = try map.value("refreshToken")
    expires = try map.value("expires", using: DateTransform())
  }
}
