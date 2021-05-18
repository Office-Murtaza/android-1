import ObjectMapper
import TrustWalletCore

extension CoinBalance: ImmutableMappable {
  init(map: Map) throws {
    let code: String = try map.value("code")
    
    guard
      let mappedType = CustomCoinType(code: code),
      let balance = Decimal(string: try map.value("balanceStr")),
      let fiatBalance = Decimal(string: try map.value("fiatBalanceStr")),
      let reservedBalance = Decimal(string: try map.value("reservedBalanceStr")),
      let reservedFiatBalance = Decimal(string: try map.value("reservedFiatBalanceStr")),
      let price = Decimal(string: try map.value("priceStr"))
    else {
      throw ObjectMapperError.couldNotMap
    }
    
    type = mappedType
    address = try map.value("address")
    self.balance = balance
    self.fiatBalance = fiatBalance
    self.reservedBalance = reservedBalance
    self.reservedFiatBalance = reservedFiatBalance
    self.price = price
    index = try map.value("idx")
    details = try map.value("details")
  }
}
