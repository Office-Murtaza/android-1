import Foundation

protocol SettingsModule: class {}
protocol SettingsModuleDelegate: class {
  func didSelectSecurity()
  func didSelectKYC(_ kyc: KYC)
  func didSelectAbout()
}
