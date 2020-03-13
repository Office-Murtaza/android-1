import ObjectMapper

extension VerificationInfo: ImmutableMappable {
  init(map: Map) throws {
    txLimit = try map.value("txLimit")
    dailyLimit = try map.value("dailyLimit")
    status = VerificationStatus(rawValue: try map.value("status"))
    message = try map.value("message")
  }
}

