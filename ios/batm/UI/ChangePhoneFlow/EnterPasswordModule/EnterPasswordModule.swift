import Foundation

protocol EnterPasswordModule: class {}
protocol EnterPasswordModuleDelegate: class {
  func didFinishEnterPassword()
  func didMatchPassword()
}
