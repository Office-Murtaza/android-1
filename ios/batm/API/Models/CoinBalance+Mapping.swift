import ObjectMapper
import TrustWalletCore

extension CoinBalance: ImmutableMappable {
  init(map: Map) throws {
    let code: String = try map.value("code")
    
    guard let mappedType = CustomCoinType(code: code) else {
      throw ObjectMapperError.couldNotMap
    }
    
    type = mappedType
    address = try map.value("address")
    balance = try map.value("balance")
    fiatBalance = try map.value("fiatBalance")
    reservedBalance = try map.value("reservedBalance")
    reservedFiatBalance = try map.value("reservedFiatBalance")
    price = try map.value("price")
    index = try map.value("idx")
  }
}
