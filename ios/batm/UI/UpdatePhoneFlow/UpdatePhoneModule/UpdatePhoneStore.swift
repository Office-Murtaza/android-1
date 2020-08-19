import Foundation
import RxSwift

enum UpdatePhoneAction: Equatable {
  case updatePhone(ValidatablePhoneNumber)
  case updateValidationState
  case makeInvalidState(String)
}

struct UpdatePhoneState: Equatable {
  
  var validatablePhone = ValidatablePhoneNumber()
  var validationState: ValidationState = .unknown
  
}

final class UpdatePhoneStore: ViewStore<UpdatePhoneAction, UpdatePhoneState> {
  
  override var initialState: UpdatePhoneState {
    return UpdatePhoneState()
  }
  
  override func reduce(state: UpdatePhoneState, action: UpdatePhoneAction) -> UpdatePhoneState {
    var state = state
    
    switch action {
    case let .updatePhone(validatablePhone): state.validatablePhone = validatablePhone
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: UpdatePhoneState) -> ValidationState {
    guard state.validatablePhone.phone.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.validatablePhone.isValid else {
      return .invalid(localize(L.CreateWallet.Form.Error.notValidPhoneNumber))
    }
    
    return .valid
  }
}
