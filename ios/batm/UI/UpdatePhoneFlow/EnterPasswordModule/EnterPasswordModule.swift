import Foundation

protocol EnterPasswordModule: class {}
protocol EnterPasswordModuleDelegate: class {
  func didMatchPassword()
}
