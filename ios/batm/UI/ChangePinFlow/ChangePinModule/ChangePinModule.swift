import Foundation

protocol ChangePinModule: class {}
protocol ChangePinModuleDelegate: class {
  func didFinishChangePin()
}
