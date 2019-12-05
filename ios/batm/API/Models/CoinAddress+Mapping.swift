import ObjectMapper

extension CoinAddress: ImmutableMappable {
  init(map: Map) throws {
    fatalError("Doesn't support such mapping")
  }
  
  func mapping(map: Map) {
    type.code >>> map["code"]
    address >>> map["address"]
  }
}

