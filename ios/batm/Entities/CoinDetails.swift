import Foundation
import TrustWalletCore

struct CoinDetails: Equatable {
  var type: CustomCoinType
  var txFee: Decimal
  var byteFee: Int?
  var scale: Int?
  var gasPrice: Int?
  var gasLimit: Int?
  var platformSwapFee: Decimal?
  var platformTradeFee: Decimal?
  var walletAddress: String
  var contractAddress: String?
  var convertedTxFee: Decimal?
}

