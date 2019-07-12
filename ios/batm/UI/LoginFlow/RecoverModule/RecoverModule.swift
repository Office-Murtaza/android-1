import Foundation

protocol RecoverModule: class {}
protocol RecoverModuleDelegate: class {
  func didCancelRecovering()
  func finishRecovering()
}
