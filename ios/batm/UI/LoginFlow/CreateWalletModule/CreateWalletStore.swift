import Foundation
import RxSwift
import PhoneNumberKit

enum CreateWalletAction: Equatable {
  case updatePhoneNumber(String?)
  case updatePassword(String?)
  case updateConfirmPassword(String?)
  case updateCode(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct CreateWalletState: Equatable {
  
  var phoneNumber: String = ""
  var password: String = ""
  var confirmPassword: String = ""
  var code = ""
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
    case let .updatePhoneNumber(phoneNumber): state.phoneNumber = phoneNumber ?? ""
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
    guard state.phoneNumber.count > 0 && state.password.count > 0 && state.confirmPassword.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let _ = try? PhoneNumberKit.default.parse(state.phoneNumber) else {
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
