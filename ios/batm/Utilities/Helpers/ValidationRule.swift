import Foundation

enum ValidationState: Equatable {
  case unknown
  case valid
  case invalid(String)
  
  var isValid: Bool {
    return self == .valid
  }
}

protocol ValidationRule {
  func validate(_ string: String) -> ValidationState
}
