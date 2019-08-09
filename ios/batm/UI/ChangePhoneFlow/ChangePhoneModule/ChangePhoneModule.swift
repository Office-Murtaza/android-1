import Foundation

protocol ChangePhoneModule: class {}
protocol ChangePhoneModuleDelegate: class {
  func didFinishChangePhone()
  func didChangePhone()
}
