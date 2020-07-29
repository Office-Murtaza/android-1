import Foundation

protocol PhoneVerificationModule: class {
  func setup(phoneNumber: String, password: String)
}
protocol PhoneVerificationModuleDelegate: class {
  func didFinishPhoneVerification(phoneNumber: String, password: String)
}
