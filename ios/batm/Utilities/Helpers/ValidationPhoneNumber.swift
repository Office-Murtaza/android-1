import Foundation

struct ValidatablePhoneNumber: Equatable {
  var phone: String = ""
  var isValid: Bool = false
  var phoneE164 = ""
}
