import ObjectMapper
import TrustWalletCore

extension CoinBalance: ImmutableMappable {
  init(map: Map) throws {
    let coin: String = try map.value("coin")
    
    guard
      let mappedType = CustomCoinType(code: coin),
      let balance = Decimal(string: try map.value("balanceStr")),
      let fiatBalance = Decimal(string: try map.value("fiatBalanceStr")),
      let reservedBalance = Decimal(string: try map.value("reservedStr")),
      let reservedFiatBalance = Decimal(string: try map.value("fiatReservedStr")),
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
