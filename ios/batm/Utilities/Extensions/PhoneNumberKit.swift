import Foundation
import PhoneNumberKit

extension PhoneNumberKit {
  
  static let `default` = PhoneNumberKit()
  
}

extension PartialFormatter {
  
  static let `default` = PartialFormatter(phoneNumberKit: PhoneNumberKit.default)
  
}
