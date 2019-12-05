import Foundation
import TrustWalletCore

struct CoinFee: Equatable {
  var type: CoinType
  var fee: Double?
  var gasPrice: Int?
  var gasLimit: Int?
}

