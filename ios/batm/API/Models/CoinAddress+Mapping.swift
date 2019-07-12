import ObjectMapper

extension CoinAddress: ImmutableMappable {
  init(map: Map) throws {
    fatalError("Doesn't support such mapping")
  }
  
  func mapping(map: Map) {
    type.code >>> map["coinCode"]
    address >>> map["publicKey"]
  }
}

