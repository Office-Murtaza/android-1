import Foundation
import RxSwift

enum CreateWalletAction: Equatable {
  case updatePhone(ValidatablePhoneNumber)
  case updatePassword(String?)
  case updateConfirmPassword(String?)
  case updateCode(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct CreateWalletState: Equatable {
  
  var validatablePhone = ValidatablePhoneNumber()
  var password: String = ""
  var confirmPassword: String = ""
  var code: String = ""
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
}

final class CreateWalletStore: ViewStore<CreateWalletAction, CreateWalletState> {
  
  override var initialState: CreateWalletState {
    return CreateWalletState()
  }
  
  override func reduce(state: CreateWalletState, action: CreateWalletAction) -> CreateWalletState {
    var state = state
    
    switch action {
    case let .updatePhone(validatablePhone): state.validatablePhone = validatablePhone
    case let .updatePassword(password): state.password = password ?? ""
    case let .updateConfirmPassword(confirmPassword): state.confirmPassword = confirmPassword ?? ""
    case let .updateCode(code): state.code = code ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    case .showCodePopup: state.shouldShowCodePopup = true
    }
    
    return state
  }
  
  private func validate(_ state: CreateWalletState) -> ValidationState {
    guard state.validatablePhone.phone.count > 0 && state.password.count > 0 && state.confirmPassword.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.validatablePhone.isValid else {
      return .invalid(localize(L.CreateWallet.Form.Error.notValidPhoneNumber))
    }
    
    guard state.password == state.confirmPassword else {
      return .invalid(localize(L.CreateWallet.Form.Error.notEqualPasswords))
    }
    
    guard !state.shouldShowCodePopup || state.code.count == 4 else {
      return .invalid(localize(L.CreateWallet.Code.Error.title))
    }
    
    return .valid
  }
}
