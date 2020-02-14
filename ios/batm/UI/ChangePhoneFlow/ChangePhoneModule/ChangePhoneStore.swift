import Foundation
import RxSwift

enum ChangePhoneAction: Equatable {
  case updatePhone(ValidatablePhoneNumber)
  case updateCode(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct ChangePhoneState: Equatable {
  
  var validatablePhone = ValidatablePhoneNumber()
  var code = ""
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
}

final class ChangePhoneStore: ViewStore<ChangePhoneAction, ChangePhoneState> {
  
  override var initialState: ChangePhoneState {
    return ChangePhoneState()
  }
  
  override func reduce(state: ChangePhoneState, action: ChangePhoneAction) -> ChangePhoneState {
    var state = state
    
    switch action {
    case let .updatePhone(validatablePhone): state.validatablePhone = validatablePhone
    case let .updateCode(code): state.code = code ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    case .showCodePopup: state.shouldShowCodePopup = true
    }
    
    return state
  }
  
  private func validate(_ state: ChangePhoneState) -> ValidationState {
    guard state.validatablePhone.phone.count > 0 else {
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
