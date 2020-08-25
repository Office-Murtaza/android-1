import ObjectMapper

extension KYC: ImmutableMappable {
  init(map: Map) throws {
    txLimit = try map.value("txLimit")
    dailyLimit = try map.value("dailyLimit")
    status = KYCStatus(rawValue: try map.value("status"))
    message = try map.value("message")
  }
}

