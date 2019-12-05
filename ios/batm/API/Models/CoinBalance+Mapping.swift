import ObjectMapper
import TrustWalletCore

extension CoinBalance: ImmutableMappable {
  init(map: Map) throws {
    let code: String = try map.value("code")
    
    guard let mappedType = CoinType(code: code) else {
      throw ObjectMapperError.couldNotMap
    }
    
    type = mappedType
    balance = try map.value("balance")
    price = try map.value("price.USD")
    index = try map.value("id")
  }
}
