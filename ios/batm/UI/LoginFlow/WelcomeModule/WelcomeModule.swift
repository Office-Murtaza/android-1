import Foundation

protocol WelcomeModule: class {}
protocol WelcomeModuleDelegate: class {
  func showCreateWalletScreen()
  func showRecoverScreen()
}
