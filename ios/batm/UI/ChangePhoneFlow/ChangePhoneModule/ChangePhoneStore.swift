import Foundation
import RxSwift

enum ChangePhoneAction: Equatable {
  case updatePhone(ValidatablePhoneNumber)
  case updateValidationState
  case makeInvalidState(String)
}

struct ChangePhoneState: Equatable {
  
  var validatablePhone = ValidatablePhoneNumber()
  var validationState: ValidationState = .unknown
  
}

final class ChangePhoneStore: ViewStore<ChangePhoneAction, ChangePhoneState> {
  
  override var initialState: ChangePhoneState {
    return ChangePhoneState()
  }
  
  override func reduce(state: ChangePhoneState, action: ChangePhoneAction) -> ChangePhoneState {
    var state = state
    
    switch action {
    case let .updatePhone(validatablePhone): state.validatablePhone = validatablePhone
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
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
    
    return .valid
  }
}
