import Foundation

enum EnterPasswordAction: Equatable {
  case updatePassword(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct EnterPasswordState: Equatable {
  
  var password: String = ""
  var validationState: ValidationState = .unknown
  
}

final class EnterPasswordStore: ViewStore<EnterPasswordAction, EnterPasswordState> {
  
  override var initialState: EnterPasswordState {
    return EnterPasswordState()
  }
  
  override func reduce(state: EnterPasswordState, action: EnterPasswordAction) -> EnterPasswordState {
    var state = state
    
    switch action {
    case let .updatePassword(password): state.password = password ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: EnterPasswordState) -> ValidationState {
    guard state.password.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    return .valid
  }
}
