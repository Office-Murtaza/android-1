import Foundation

protocol ErrorModule: AnyObject {
  func setup(with type: ErrorType)
}
protocol ErrorModuleDelegate: AnyObject {
  func didFinishError()
}
