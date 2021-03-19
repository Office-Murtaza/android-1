import Foundation

protocol EnterPasswordModule: AnyObject {}
protocol EnterPasswordModuleDelegate: AnyObject {
  func didMatchPassword()
}
