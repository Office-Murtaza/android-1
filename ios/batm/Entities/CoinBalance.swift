import Foundation
import TrustWalletCore

struct CoinBalance: Equatable {
  let type: CoinType
  let balance: Double
  let price: Double
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
