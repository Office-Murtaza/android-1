import Foundation
import RxSwift

enum PhoneVerificationAction: Equatable {
  case setupPhoneNumber(String)
  case setupPassword(String)
  case updateCorrectCode(String)
  case updateCode(String)
  case updateValidationState
  case makeInvalidState(String)
}

struct PhoneVerificationState: Equatable {
  
  var phoneNumber: String = ""
  var password: String = ""
  var correctCode: String = ""
  var code: String = ""
  var validationState: ValidationState = .unknown
  
}

final class PhoneVerificationStore: ViewStore<PhoneVerificationAction, PhoneVerificationState> {
  
  override var initialState: PhoneVerificationState {
    return PhoneVerificationState()
  }
  
  override func reduce(state: PhoneVerificationState, action: PhoneVerificationAction) -> PhoneVerificationState {
    var state = state
    
    switch action {
    case let .setupPhoneNumber(phoneNumber): state.phoneNumber = phoneNumber
    case let .setupPassword(password): state.password = password
    case let .updateCorrectCode(correctCode): state.correctCode = correctCode
    case let .updateCode(code): state.code = code
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: PhoneVerificationState) -> ValidationState {
    guard state.code.count == 4 && state.code == state.correctCode else {
      return .invalid(localize(L.PhoneVerification.Error.invalidCode))
    }
    
    return .valid
  }
}
