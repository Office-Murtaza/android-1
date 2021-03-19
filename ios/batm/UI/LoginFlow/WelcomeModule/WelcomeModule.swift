import Foundation

protocol WelcomeModule: AnyObject {}
protocol WelcomeModuleDelegate: AnyObject {
  func showCreateWalletScreen()
  func showRecoverScreen()
  func showContactSupportAlert()
}
