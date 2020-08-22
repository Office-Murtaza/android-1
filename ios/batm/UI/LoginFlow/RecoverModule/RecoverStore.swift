import Foundation
import RxSwift
import PhoneNumberKit

enum RecoverAction: Equatable {
  case updatePhoneNumber(String?)
  case updatePassword(String?)
  case updatePhoneNumberError(String?)
  case updatePasswordError(String?)
  case updateValidationState
}

struct RecoverState: Equatable {
  
  var phoneNumber: String = ""
  var password: String = ""
  var phoneNumberError: String?
  var passwordError: String?
  var validationState: ValidationState = .unknown
  
  var phoneE164: String {
    guard let phoneNumber = try? PhoneNumberKit.default.parse(phoneNumber) else { return "" }
    
    return PhoneNumberKit.default.format(phoneNumber, toType: .e164)
  }
  
  var isAllFieldsNotEmpty: Bool {
    return phoneNumber.count > 0 && password.count > 0
  }
  
}

final class RecoverStore: ViewStore<RecoverAction, RecoverState> {
  
  override var initialState: RecoverState {
    return RecoverState()
  }
  
  override func reduce(state: RecoverState, action: RecoverAction) -> RecoverState {
    var state = state
    
    switch action {
    case let .updatePhoneNumber(phoneNumber):
      state.phoneNumber = PartialFormatter.default.formatPartial(phoneNumber ?? "")
      state.phoneNumberError = nil
    case let .updatePassword(password):
      state.password = password ?? ""
      state.passwordError = nil
    case let .updatePhoneNumberError(phoneNumberError): state.phoneNumberError = phoneNumberError
    case let .updatePasswordError(passwordError): state.passwordError = passwordError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout RecoverState) {
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
    } else {
      state.passwordError = nil
    }
  }
}
