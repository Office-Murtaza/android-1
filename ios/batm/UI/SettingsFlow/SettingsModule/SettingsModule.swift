import Foundation

protocol SettingsModule: class {}
protocol SettingsModuleDelegate: class {
  func didSelectPhone(_ phoneNumber: PhoneNumber)
  func didSelectChangePassword()
  func didSelectChangePin()
  func didSelectVerification(_ info: VerificationInfo)
  func didSelectShowSeedPhrase()
  func didSelectUnlink()
}
