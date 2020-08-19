import Foundation

protocol UpdatePhoneModule: class {}
protocol UpdatePhoneModuleDelegate: class {
  func didUpdatePhone()
}
