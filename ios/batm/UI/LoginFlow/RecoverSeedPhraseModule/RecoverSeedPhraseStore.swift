import Foundation

enum RecoverSeedPhraseAction: Equatable {
  case setupPhoneNumber(String)
  case setupPassword(String)
  case updateSeedPhrase([String])
  case updateSeedPhraseError(String?)
  case updateValidationState
}

struct RecoverSeedPhraseState: Equatable {
  
  var phoneNumber: String = ""
  var password: String = ""
  var seedPhrase: [String] = []
  var seedPhraseError: String?
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
    case let .updateSeedPhrase(seedPhrase):
      state.seedPhrase = seedPhrase
      state.seedPhraseError = nil
    case let .updateSeedPhraseError(seedPhraseError): state.seedPhraseError = seedPhraseError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout RecoverSeedPhraseState) {
    state.validationState = .valid
    
    if state.seedPhrase.count != BTMWallet.seedPhraseLength {
      let errorString = localize(L.RecoverSeedPhrase.Form.Error.notValidLength)
      state.validationState = .invalid(errorString)
      state.seedPhraseError = errorString
    }
  }

}
