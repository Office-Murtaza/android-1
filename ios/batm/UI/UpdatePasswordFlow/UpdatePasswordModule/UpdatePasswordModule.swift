import Foundation

protocol UpdatePasswordModule: AnyObject {}
protocol UpdatePasswordModuleDelegate: AnyObject {
  func didUpdatePassword()
}
