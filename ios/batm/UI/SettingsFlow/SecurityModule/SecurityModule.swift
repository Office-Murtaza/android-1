import Foundation

protocol SecurityModule: AnyObject {}
protocol SecurityModuleDelegate: AnyObject {
  func didSelectUpdatePhone(_ phoneNumber: String)
  func didSelectUpdatePassword()
  func didSelectUpdatePIN(_ pinCode: String)
  func didSelectSeedPhrase()
  func didSelectUnlink()
}
