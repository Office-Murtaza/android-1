import ObjectMapper

extension CoinsBalance: ImmutableMappable {
  init(map: Map) throws {
    guard let totalBalance = Decimal(string: try map.value("totalBalanceStr")) else {
      throw ObjectMapperError.couldNotMap
    }
    
    self.totalBalance = totalBalance
    coins = try map.value("coins")
  }
}

