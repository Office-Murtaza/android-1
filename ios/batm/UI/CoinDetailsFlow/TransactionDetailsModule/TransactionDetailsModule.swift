import Foundation

protocol TransactionDetailsModule: class {
  func setup(with details: TransactionDetails)
}
protocol TransactionDetailsModuleDelegate: class {
  func didFinishTransactionDetails()
}
