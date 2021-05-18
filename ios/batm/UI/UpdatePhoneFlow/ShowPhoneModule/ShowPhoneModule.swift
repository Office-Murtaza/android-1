import Foundation

protocol ShowPhoneModule: AnyObject {
  func setup(with phoneNumber: PhoneNumber)
}
protocol ShowPhoneModuleDelegate: AnyObject {
  func didSelectUpdatePhone(phoneNumber: String)
}
