import Foundation

protocol UpdatePhoneModule: AnyObject {
  func setup(oldPhoneNumber: String)
}
protocol UpdatePhoneModuleDelegate: AnyObject {
  func didNotMatchNewPhoneNumber(_ phoneNumber: String)
}
