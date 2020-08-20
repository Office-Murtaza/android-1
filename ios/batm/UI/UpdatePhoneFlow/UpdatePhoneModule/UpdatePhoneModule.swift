import Foundation

protocol UpdatePhoneModule: class {
  func setup(oldPhoneNumber: String)
}
protocol UpdatePhoneModuleDelegate: class {
  func didNotMatchNewPhoneNumber(_ phoneNumber: String)
}
