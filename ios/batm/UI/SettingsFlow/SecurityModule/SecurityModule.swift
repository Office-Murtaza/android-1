import Foundation

protocol SecurityModule: class {}
protocol SecurityModuleDelegate: class {
  func didSelectUpdatePhone(_ phoneNumber: PhoneNumber)
  func didSelectUpdatePassword()
  func didSelectUpdatePIN(_ pinCode: String)
  func didSelectSeedPhrase()
  func didSelectUnlinkWallet()
}
