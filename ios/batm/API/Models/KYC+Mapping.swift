import ObjectMapper

extension KYC: ImmutableMappable {
  init(map: Map) throws {
    txLimit = try map.value("txLimit", using: DecimalDoubleTransform())
    dailyLimit = try map.value("dailyLimit", using: DecimalDoubleTransform())
    status = KYCStatus(rawValue: try map.value("status"))
    message = try map.value("message")
  }
}

