import Foundation
import TrustWalletCore

struct CoinBalance: Equatable {
  let type: CoinType
  let balance: Double
  let price: Double
}

extension CoinBalance {
  var maxValue: Double {
    let maxValue = max(0, balance - type.fee)
    
    switch type {
    case .ethereum: return min(9, maxValue)
    default: return maxValue
    }
  }
}
