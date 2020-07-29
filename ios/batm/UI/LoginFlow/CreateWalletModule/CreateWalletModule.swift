import Foundation

protocol CreateWalletModule: class {}
protocol CreateWalletModuleDelegate: class {
  func didCancelCreatingWallet()
  func finishCreatingWallet(phoneNumber: String, password: String)
}
