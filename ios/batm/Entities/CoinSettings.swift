import Foundation
import TrustWalletCore

struct CoinSettings: Equatable {
  var type: CustomCoinType
  var txFee: Decimal
  var recallFee: Decimal?
  var byteFee: Decimal?
  var gasPrice: Int?
  var gasLimit: Int?
  var profitExchange: Decimal
  var walletAddress: String
  var contractAddress: String?
}

