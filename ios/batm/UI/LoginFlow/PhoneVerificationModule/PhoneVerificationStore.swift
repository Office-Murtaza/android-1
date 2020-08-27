import Foundation
import RxSwift

enum PhoneVerificationMode {
  case create
  case update
}

enum PhoneVerificationAction: Equatable {
  case setupMode(PhoneVerificationMode)
  case setupPhoneNumber(String)
  case updateCorrectCode(String)
  case updateCode(String)
  case updateCodeError(String?)
  case updateValidationState
}

struct PhoneVerificationState: Equatable {
  
  var mode: PhoneVerificationMode = .create
  var phoneNumber: String = ""
  var correctCode: String = ""
  var code: String = ""
  var codeError: String?
  var validationState: ValidationState = .unknown
  
  var isCodeFilled: Bool {
    return code.count == 4
  }
  
}

final class PhoneVerificationStore: ViewStore<PhoneVerificationAction, PhoneVerificationState> {
  
  override var initialState: PhoneVerificationState {
    return PhoneVerificationState()
  }
  
  override func reduce(state: PhoneVerificationState, action: PhoneVerificationAction) -> PhoneVerificationState {
    var state = state
    
    switch action {
    case let .setupMode(mode): state.mode = mode
    case let .setupPhoneNumber(phoneNumber): state.phoneNumber = phoneNumber
    case let .updateCorrectCode(correctCode): state.correctCode = correctCode
    case let .updateCode(code):
      state.code = code
      state.codeError = nil
    case let .updateCodeError(codeError): state.codeError = codeError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout PhoneVerificationState) {
    state.validationState = .valid
    
    if !state.isCodeFilled || state.code != state.correctCode {
      let errorString = localize(L.PhoneVerification.Error.invalidCode)
      state.codeError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.codeError = nil
    }
  }
}
