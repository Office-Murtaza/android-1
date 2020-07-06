import Foundation
import TrustWalletCore

struct CoinSettings: Equatable {
  var type: CustomCoinType
  var txFee: Double
  var byteFee: Double?
  var gasPrice: Int?
  var gasLimit: Int?
  var profitExchange: Double
  var walletAddress: String
  var contractAddress: String?
}

