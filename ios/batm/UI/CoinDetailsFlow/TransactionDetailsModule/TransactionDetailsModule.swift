import Foundation
import TrustWalletCore

protocol TransactionDetailsModule: class {
  func setup(with details: TransactionDetails, for type: CoinType)
}
protocol TransactionDetailsModuleDelegate: class {
  func didFinishTransactionDetails()
}
