import Foundation

enum UpdatePasswordAction: Equatable {
  case updateOldPassword(String?)
  case updateNewPassword(String?)
  case updateConfirmNewPassword(String?)
  case updateOldPasswordError(String?)
  case updateNewPasswordError(String?)
  case updateConfirmNewPasswordError(String?)
  case updateValidationState
}

struct UpdatePasswordState: Equatable {
  
  var oldPassword: String = ""
  var newPassword: String = ""
  var confirmNewPassword: String = ""
  var oldPasswordError: String?
  var newPasswordError: String?
  var confirmNewPasswordError: String?
  var validationState: ValidationState = .unknown
  
  var isAllFieldsNotEmpty: Bool {
    return oldPassword.count > 0
      && newPassword.count >= GlobalConstants.minPasswordLength
      && confirmNewPassword.count >= GlobalConstants.minPasswordLength
  }
  
}

final class UpdatePasswordStore: ViewStore<UpdatePasswordAction, UpdatePasswordState> {
  
  override var initialState: UpdatePasswordState {
    return UpdatePasswordState()
  }
  
  override func reduce(state: UpdatePasswordState, action: UpdatePasswordAction) -> UpdatePasswordState {
    var state = state
    
    switch action {
    case let .updateOldPassword(password):
      state.oldPassword = password ?? ""
      state.oldPasswordError = nil
    case let .updateNewPassword(newPassword):
      if let newPassword = newPassword?.prefix(GlobalConstants.maxPasswordLength) {
        state.newPassword = String(newPassword)
      } else {
        state.newPassword = ""
      }
      state.newPasswordError = nil
    case let .updateConfirmNewPassword(confirmNewPassword):
      if let confirmNewPassword = confirmNewPassword?.prefix(GlobalConstants.maxPasswordLength) {
        state.confirmNewPassword = String(confirmNewPassword)
      } else {
        state.confirmNewPassword = ""
      }
      state.confirmNewPasswordError = nil
    case let .updateOldPasswordError(oldPasswordError): state.oldPasswordError = oldPasswordError
    case let .updateNewPasswordError(newPasswordError): state.newPasswordError = newPasswordError
    case let .updateConfirmNewPasswordError(confirmNewPasswordError): state.confirmNewPasswordError = confirmNewPasswordError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout UpdatePasswordState) {
    state.validationState = .valid
    
    if state.oldPassword.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.oldPasswordError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.oldPasswordError = nil
    }
    
    if state.newPassword.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.newPasswordError = errorString
      state.validationState = .invalid(errorString)
    } else if state.newPassword.count < 6 || state.newPassword.count > 15 {
      let errorString = localize(L.CreateWallet.Form.Error.notValidPassword)
      state.newPasswordError = errorString
      state.validationState = .invalid(errorString)
    } else if state.oldPassword == state.newPassword {
      let errorString = localize(L.UpdatePassword.Form.Error.samePassword)
      state.newPasswordError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.newPasswordError = nil
    }
    
    if state.confirmNewPassword.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.confirmNewPasswordError = errorString
      state.validationState = .invalid(errorString)
    } else if state.confirmNewPassword != state.newPassword {
      let errorString = localize(L.CreateWallet.Form.Error.notEqualPasswords)
      state.confirmNewPasswordError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.confirmNewPasswordError = nil
    }
  }
}
