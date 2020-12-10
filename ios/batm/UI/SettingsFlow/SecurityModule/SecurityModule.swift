import Foundation

protocol SecurityModule: class {}
protocol SecurityModuleDelegate: class {
  func didSelectUpdatePhone(_ phoneNumber: String)
  func didSelectUpdatePassword()
  func didSelectUpdatePIN(_ pinCode: String)
  func didSelectSeedPhrase()
  func didSelectUnlink()
}
