import ObjectMapper

extension CoinsBalance: ImmutableMappable {
  init(map: Map) throws {
    totalBalance = try map.value("totalBalance")
    coins = try map.value("coins")
  }
}

