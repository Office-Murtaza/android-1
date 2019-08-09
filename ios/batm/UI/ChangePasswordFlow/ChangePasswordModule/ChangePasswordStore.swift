import Foundation

enum ChangePasswordAction: Equatable {
  case updateOldPassword(String?)
  case updateNewPassword(String?)
  case updateConfirmNewPassword(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct ChangePasswordState: Equatable {
  
  var oldPassword: String = ""
  var newPassword: String = ""
  var confirmNewPassword: String = ""
  var validationState: ValidationState = .unknown
  
}

final class ChangePasswordStore: ViewStore<ChangePasswordAction, ChangePasswordState> {
  
  override var initialState: ChangePasswordState {
    return ChangePasswordState()
  }
  
  override func reduce(state: ChangePasswordState, action: ChangePasswordAction) -> ChangePasswordState {
    var state = state
    
    switch action {
    case let .updateOldPassword(password): state.oldPassword = password ?? ""
    case let .updateNewPassword(password): state.newPassword = password ?? ""
    case let .updateConfirmNewPassword(password): state.confirmNewPassword = password ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: ChangePasswordState) -> ValidationState {
    guard state.oldPassword.count > 0 && state.newPassword.count > 0 && state.confirmNewPassword.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.newPassword == state.confirmNewPassword else {
      return .invalid(localize(L.CreateWallet.Form.Error.notEqualPasswords))
    }
    
    return .valid
  }
}
