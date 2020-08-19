import Foundation

enum EnterPasswordAction: Equatable {
  case updatePassword(String?)
  case updatePasswordError(String?)
  case updateValidationState
}

struct EnterPasswordState: Equatable {
  
  var password: String = ""
  var passwordError: String?
  var validationState: ValidationState = .unknown
  
}

final class EnterPasswordStore: ViewStore<EnterPasswordAction, EnterPasswordState> {
  
  override var initialState: EnterPasswordState {
    return EnterPasswordState()
  }
  
  override func reduce(state: EnterPasswordState, action: EnterPasswordAction) -> EnterPasswordState {
    var state = state
    
    switch action {
    case let .updatePassword(password):
      state.password = password ?? ""
      state.passwordError = nil
    case let .updatePasswordError(passwordError): state.passwordError = passwordError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout EnterPasswordState) {
    state.validationState = .valid
    
    if state.password.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.passwordError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.passwordError = nil
    }
  }
}
