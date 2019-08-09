import Foundation

enum ChangePinAction: Equatable {
  case updateOldPin(String?)
  case updateNewPin(String?)
  case updateConfirmNewPin(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct ChangePinState: Equatable {
  
  var oldPin: String = ""
  var newPin: String = ""
  var confirmNewPin: String = ""
  var validationState: ValidationState = .unknown
  
}

final class ChangePinStore: ViewStore<ChangePinAction, ChangePinState> {
  
  override var initialState: ChangePinState {
    return ChangePinState()
  }
  
  override func reduce(state: ChangePinState, action: ChangePinAction) -> ChangePinState {
    var state = state
    
    switch action {
    case let .updateOldPin(pin): state.oldPin = pin ?? ""
    case let .updateNewPin(pin): state.newPin = pin ?? ""
    case let .updateConfirmNewPin(pin): state.confirmNewPin = pin ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: ChangePinState) -> ValidationState {
    guard state.oldPin.count > 0 && state.newPin.count > 0 && state.confirmNewPin.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.oldPin.count == 6 && state.newPin.count == 6 && state.confirmNewPin.count == 6 else {
      return .invalid(localize(L.ChangePin.Form.Error.wrongLength))
    }
    
    guard state.newPin == state.confirmNewPin else {
      return .invalid(localize(L.ChangePin.Form.Error.notEqualPins))
    }
    
    return .valid
  }
}
