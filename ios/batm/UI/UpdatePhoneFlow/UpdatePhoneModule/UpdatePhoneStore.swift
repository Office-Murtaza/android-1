import Foundation
import RxSwift
import PhoneNumberKit

enum UpdatePhoneAction: Equatable {
  case setupOldPhoneNumber(String)
  case updatePhoneNumber(String?)
  case updatePhoneNumberError(String?)
  case updateValidationState
}

struct UpdatePhoneState: Equatable {
  
  var oldPhoneNumber: String = localize(L.Phone.Default.prefix)
  var phoneNumber: String = localize(L.Phone.Default.prefix)
  var phoneNumberError: String?
  var validationState: ValidationState = .unknown
  
  var phoneE164: String {
    guard let phoneNumber = try? PhoneNumberKit.default.parse(phoneNumber) else { return "" }
    
    return PhoneNumberKit.default.format(phoneNumber, toType: .e164)
  }
  
}

final class UpdatePhoneStore: ViewStore<UpdatePhoneAction, UpdatePhoneState> {
  
  override var initialState: UpdatePhoneState {
    return UpdatePhoneState()
  }
  
  override func reduce(state: UpdatePhoneState, action: UpdatePhoneAction) -> UpdatePhoneState {
    var state = state
    
    switch action {
    case let .setupOldPhoneNumber(oldPhoneNumber): state.oldPhoneNumber = oldPhoneNumber
    case let .updatePhoneNumber(phoneNumber):
      state.phoneNumber = PartialFormatter.default.formatPartial(phoneNumber ?? "")
      state.phoneNumberError = nil
    case let .updatePhoneNumberError(phoneNumberError): state.phoneNumberError = phoneNumberError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout UpdatePhoneState) {
    state.validationState = .valid
    
    if state.phoneNumber.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.phoneNumberError = errorString
      state.validationState = .invalid(errorString)
    } else if state.phoneE164.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.notValidPhoneNumber)
      state.phoneNumberError = errorString
      state.validationState = .invalid(errorString)
    } else if state.phoneE164 == state.oldPhoneNumber {
      let errorString = localize(L.UpdatePhone.Form.Error.samePhone)
      state.phoneNumberError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.phoneNumberError = nil
    }
  }
}
