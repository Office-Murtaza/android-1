import Foundation

protocol ChangePasswordModule: class {}
protocol ChangePasswordModuleDelegate: class {
  func didFinishChangePassword()
  func didChangePassword()
}
