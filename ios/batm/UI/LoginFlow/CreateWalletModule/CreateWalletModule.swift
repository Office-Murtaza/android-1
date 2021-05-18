import Foundation

protocol CreateWalletModule: AnyObject {}
protocol CreateWalletModuleDelegate: AnyObject {
  func didCancelCreatingWallet()
  func finishCreatingWallet(phoneNumber: String, password: String)
}
