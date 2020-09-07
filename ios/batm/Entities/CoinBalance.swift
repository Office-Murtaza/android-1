import Foundation
import TrustWalletCore

struct CoinBalance: Equatable {
  let type: CustomCoinType
  let address: String
  let balance: Decimal
  let fiatBalance: Decimal
  let reservedBalance: Decimal
  let reservedFiatBalance: Decimal
  let price: Decimal
  let index: Int
}

extension CoinBalance: Comparable {
  static func < (lhs: CoinBalance, rhs: CoinBalance) -> Bool {
    if lhs.index == rhs.index {
      return lhs.type.verboseValue < rhs.type.verboseValue
    }
    
    return lhs.index < rhs.index
  }
}
