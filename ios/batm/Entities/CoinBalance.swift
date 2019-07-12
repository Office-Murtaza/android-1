import Foundation
import TrustWalletCore

struct CoinBalance: Equatable {
  let type: CoinType
  let balance: Double
  let price: Double
}
