import Foundation

protocol UpdatePasswordModule: class {}
protocol UpdatePasswordModuleDelegate: class {
  func didUpdatePassword()
}
