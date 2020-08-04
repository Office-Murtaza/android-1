import Foundation
import RxSwift
import PhoneNumberKit

enum RecoverAction: Equatable {
  case updatePhoneNumber(String?)
  case updatePassword(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct RecoverState: Equatable {
  
  var phoneNumber: String = ""
  var password: String = ""
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
    case let .updatePhoneNumber(phoneNumber): state.phoneNumber = PartialFormatter.default.formatPartial(phoneNumber ?? "")
    case let .updatePassword(password): state.password = password ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: RecoverState) -> ValidationState {
    guard state.isAllFieldsNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.phoneE164.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.notValidPhoneNumber))
    }
    
    return .valid
  }
}
