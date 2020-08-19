import Foundation

protocol ShowPhoneModule: class {
  func setup(with phoneNumber: PhoneNumber)
}
protocol ShowPhoneModuleDelegate: class {
  func didSelectUpdatePhone()
}
