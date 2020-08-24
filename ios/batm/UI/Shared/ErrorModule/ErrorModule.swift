import Foundation

protocol ErrorModule: class {
  func setup(with type: ErrorType)
}
protocol ErrorModuleDelegate: class {
  func didFinishError()
}
