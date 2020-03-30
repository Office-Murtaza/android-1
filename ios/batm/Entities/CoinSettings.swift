import Foundation
import TrustWalletCore

struct CoinSettings: Equatable {
  var txFee: Double
  var byteFee: Double?
  var gasPrice: Int?
  var gasLimit: Int?
  var profitC2C: Double
}

