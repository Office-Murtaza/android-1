import Foundation

protocol SettingsModule: class {}
protocol SettingsModuleDelegate: class {
  func didSelectWallet()
  func didSelectSecurity()
  func didSelectKYC(_ kyc: KYC)
  func didSelectAbout()
  func didSelectSupport()
  func didSelectNotifications()
}
