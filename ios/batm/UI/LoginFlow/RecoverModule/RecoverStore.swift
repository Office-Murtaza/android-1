import Foundation
import RxSwift

enum RecoverAction: Equatable {
  case updatePhone(ValidatablePhoneNumber)
  case updatePassword(String?)
  case updateCode(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct RecoverState: Equatable {
  
  var validatablePhone = ValidatablePhoneNumber()
  var password: String = ""
  var code = ""
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
}

final class RecoverStore: ViewStore<RecoverAction, RecoverState> {
  
  override var initialState: RecoverState {
    return RecoverState()
  }
  
  override func reduce(state: RecoverState, action: RecoverAction) -> RecoverState {
    var state = state
    
    switch action {
    case let .updatePhone(validatablePhone): state.validatablePhone = validatablePhone
    case let .updatePassword(password): state.password = password ?? ""
    case let .updateCode(code): state.code = code ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    case .showCodePopup: state.shouldShowCodePopup = true
    }
    
    return state
  }
  
  private func validate(_ state: RecoverState) -> ValidationState {
    guard state.validatablePhone.phone.count > 0 && state.password.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.validatablePhone.isValid else {
      return .invalid(localize(L.CreateWallet.Form.Error.notValidPhoneNumber))
    }
    
    guard !state.shouldShowCodePopup || state.code.count == 4 else {
      return .invalid(localize(L.CreateWallet.Code.Error.title))
    }
    
    return .valid
  }
}
