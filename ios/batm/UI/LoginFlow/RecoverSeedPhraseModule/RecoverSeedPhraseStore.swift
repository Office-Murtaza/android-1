import Foundation

enum RecoverSeedPhraseAction: Equatable {
  case setupPhoneNumber(String)
  case setupPassword(String)
  case updateSeedPhrase([String])
  case updateValidationState
  case makeInvalidState(String)
}

struct RecoverSeedPhraseState: Equatable {
  
  var phoneNumber: String = ""
  var password: String = ""
  var seedPhrase: [String] = []
  var validationState: ValidationState = .unknown
  
  var fullSeedPhrase: String {
    return seedPhrase.joined(separator: " ")
  }
  
}

final class RecoverSeedPhraseStore: ViewStore<RecoverSeedPhraseAction, RecoverSeedPhraseState> {
  
  override var initialState: RecoverSeedPhraseState {
    return RecoverSeedPhraseState()
  }
  
  override func reduce(state: RecoverSeedPhraseState, action: RecoverSeedPhraseAction) -> RecoverSeedPhraseState {
    var state = state
    
    switch action {
    case let .setupPhoneNumber(phoneNumber): state.phoneNumber = phoneNumber
    case let .setupPassword(password): state.password = password
    case let .updateSeedPhrase(seedPhrase): state.seedPhrase = seedPhrase
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: RecoverSeedPhraseState) -> ValidationState {
    guard state.seedPhrase.count == BTMWallet.seedPhraseLength else {
      return .invalid(localize(L.RecoverSeedPhrase.Form.Error.notValidLength))
    }
    
    return .valid
  }
}
