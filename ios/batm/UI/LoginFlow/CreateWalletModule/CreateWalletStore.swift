import Foundation
import RxSwift
import PhoneNumberKit

enum CreateWalletAction: Equatable {
  case updatePhoneNumber(String?)
  case updatePassword(String?)
  case updateConfirmPassword(String?)
  case updatePhoneNumberError(String?)
  case updatePasswordError(String?)
  case updateConfirmPasswordError(String?)
  case updateValidationState
}

struct CreateWalletState: Equatable {
  
  var phoneNumber: String = ""
  var password: String = ""
  var confirmPassword: String = ""
  var phoneNumberError: String?
  var passwordError: String?
  var confirmPasswordError: String?
  var validationState: ValidationState = .unknown
  
  var phoneE164: String {
    guard let phoneNumber = try? PhoneNumberKit.default.parse(phoneNumber) else { return "" }
    
    return PhoneNumberKit.default.format(phoneNumber, toType: .e164)
  }
  
  var isAllFieldsNotEmpty: Bool {
    return phoneNumber.count > 0
      && password.count >= GlobalConstants.minPasswordLength
      && confirmPassword.count >= GlobalConstants.minPasswordLength
  }
  
}

final class CreateWalletStore: ViewStore<CreateWalletAction, CreateWalletState> {
  
  override var initialState: CreateWalletState {
    return CreateWalletState()
  }
  
  override func reduce(state: CreateWalletState, action: CreateWalletAction) -> CreateWalletState {
    var state = state
    
    switch action {
    case let .updatePhoneNumber(phoneNumber):
      state.phoneNumber = PartialFormatter.default.formatPartial(phoneNumber ?? "")
      state.phoneNumberError = nil
    case let .updatePassword(password):
      if let password = password?.prefix(GlobalConstants.maxPasswordLength) {
        state.password = String(password)
      } else {
        state.password = ""
      }
      state.passwordError = nil
    case let .updateConfirmPassword(confirmPassword):
      if let confirmPassword = confirmPassword?.prefix(GlobalConstants.maxPasswordLength) {
        state.confirmPassword = String(confirmPassword)
      } else {
        state.confirmPassword = ""
      }
      state.confirmPasswordError = nil
    case let .updatePhoneNumberError(phoneNumberError): state.phoneNumberError = phoneNumberError
    case let .updatePasswordError(passwordError): state.passwordError = passwordError
    case let .updateConfirmPasswordError(confirmPasswordError): state.confirmPasswordError = confirmPasswordError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout CreateWalletState) {
    state.validationState = .valid
    
    if state.phoneNumber.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.phoneNumberError = errorString
      state.validationState = .invalid(errorString)
    } else if state.phoneE164.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.notValidPhoneNumber)
      state.phoneNumberError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.phoneNumberError = nil
    }
    
    if state.password.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.passwordError = errorString
      state.validationState = .invalid(errorString)
    } else if state.password.count < GlobalConstants.minPasswordLength || state.password.count > GlobalConstants.maxPasswordLength {
      let errorString = localize(L.CreateWallet.Form.Error.notValidPassword)
      state.passwordError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.passwordError = nil
    }
    
    if state.confirmPassword.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.confirmPasswordError = errorString
      state.validationState = .invalid(errorString)
    } else if state.password != state.confirmPassword {
      let errorString = localize(L.CreateWallet.Form.Error.notEqualPasswords)
      state.confirmPasswordError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.confirmPasswordError = nil
    }
  }
}
