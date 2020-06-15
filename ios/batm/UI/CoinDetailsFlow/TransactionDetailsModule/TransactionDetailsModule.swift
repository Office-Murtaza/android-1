import Foundation
import TrustWalletCore

protocol TransactionDetailsModule: class {
  func setup(with details: TransactionDetails, for type: CustomCoinType)
}
protocol TransactionDetailsModuleDelegate: class {
  func didFinishTransactionDetails()
}
