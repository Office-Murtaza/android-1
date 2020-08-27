import Foundation

protocol PhoneVerificationModule: class {
  func setup(phoneNumber: String, for mode: PhoneVerificationMode)
}
protocol PhoneVerificationModuleDelegate: class {
  func didFinishPhoneVerification(phoneNumber: String)
}
