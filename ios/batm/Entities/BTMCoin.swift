import Foundation
import TrustWalletCore

struct BTMCoin: Equatable {
  let type: CoinType
  let privateKey: String
  let publicKey: String
  let isVisible: Bool
}
