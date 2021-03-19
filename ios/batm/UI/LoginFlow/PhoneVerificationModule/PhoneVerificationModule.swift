import Foundation

protocol PhoneVerificationModule: AnyObject {
  func setup(phoneNumber: String, for mode: PhoneVerificationMode)
}
protocol PhoneVerificationModuleDelegate: AnyObject {
  func didFinishPhoneVerification(phoneNumber: String)
}
