import Foundation

protocol SecurityModule: class {}
protocol SecurityModuleDelegate: class {
  func didSelectUpdatePhone(_ phoneNumber: PhoneNumber)
  func didSelectUpdatePassword()
  func didSelectUpdatePIN()
  func didSelectSeedPhrase()
  func didSelectUnlinkWallet()
}
