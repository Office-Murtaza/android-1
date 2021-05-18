import Foundation

protocol RecoverModule: AnyObject {}
protocol RecoverModuleDelegate: AnyObject {
  func didCancelRecovering()
  func finishRecovering(phoneNumber: String, password: String)
}
