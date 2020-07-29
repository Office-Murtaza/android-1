import Foundation
import RxSwift
import PhoneNumberKit

enum CreateWalletAction: Equatable {
  case updatePhoneNumber(String?)
  case updatePassword(String?)
  case updateConfirmPassword(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct CreateWalletState: Equatable {
  
  var phoneNumber: String = ""
  var password: String = ""
  var confirmPassword: String = ""
  var validationState: ValidationState = .unknown
  
  var phoneE164: String {
    guard let phoneNumber = try? PhoneNumberKit.default.parse(phoneNumber) else { return "" }
    
    return PhoneNumberKit.default.format(phoneNumber, toType: .e164)
  }
  
}

final class CreateWalletStore: ViewStore<CreateWalletAction, CreateWalletState> {
  
  override var initialState: CreateWalletState {
    return CreateWalletState()
  }
  
  override func reduce(state: CreateWalletState, action: CreateWalletAction) -> CreateWalletState {
    var state = state
    
    switch action {
    case let .updatePhoneNumber(phoneNumber): state.phoneNumber = PartialFormatter.default.formatPartial(phoneNumber ?? "")
    case let .updatePassword(password): state.password = password ?? ""
    case let .updateConfirmPassword(confirmPassword): state.confirmPassword = confirmPassword ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: CreateWalletState) -> ValidationState {
    guard state.phoneNumber.count > 0 && state.password.count > 0 && state.confirmPassword.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.phoneE164.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.notValidPhoneNumber))
    }
    
    guard state.password.count >= 6 && state.password.count <= 20 else {
      return .invalid(localize(L.CreateWallet.Form.Error.notValidPassword))
    }
    
    guard state.password == state.confirmPassword else {
      return .invalid(localize(L.CreateWallet.Form.Error.notEqualPasswords))
    }
    
    return .valid
  }
}
