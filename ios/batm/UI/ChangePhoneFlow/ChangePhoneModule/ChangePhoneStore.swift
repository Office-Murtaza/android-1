import Foundation
import RxSwift
import PhoneNumberKit

enum ChangePhoneAction: Equatable {
  case updatePhoneNumber(String?)
  case updateCode(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct ChangePhoneState: Equatable {
  
  var phoneNumber: String = ""
  var code = ""
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
}

final class ChangePhoneStore: ViewStore<ChangePhoneAction, ChangePhoneState> {
  
  override var initialState: ChangePhoneState {
    return ChangePhoneState()
  }
  
  override func reduce(state: ChangePhoneState, action: ChangePhoneAction) -> ChangePhoneState {
    var state = state
    
    switch action {
    case let .updatePhoneNumber(phoneNumber): state.phoneNumber = phoneNumber ?? ""
    case let .updateCode(code): state.code = code ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    case .showCodePopup: state.shouldShowCodePopup = true
    }
    
    return state
  }
  
  private func validate(_ state: ChangePhoneState) -> ValidationState {
    guard state.phoneNumber.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let _ = try? PhoneNumberKit.default.parse(state.phoneNumber) else {
      return .invalid(localize(L.CreateWallet.Form.Error.notValidPhoneNumber))
    }
    
    guard !state.shouldShowCodePopup || state.code.count == 4 else {
      return .invalid(localize(L.CreateWallet.Code.Error.title))
    }
    
    return .valid
  }
}
