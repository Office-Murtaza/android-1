import Foundation

enum RecoverSeedPhraseAction: Equatable {
  case updateSeedPhrase([String])
  case updateValidationState
  case makeInvalidState(String)
}

struct RecoverSeedPhraseState: Equatable {
  
  var seedPhrase: [String] = []
  var validationState: ValidationState = .unknown
  
}

final class RecoverSeedPhraseStore: ViewStore<RecoverSeedPhraseAction, RecoverSeedPhraseState> {
  
  override var initialState: RecoverSeedPhraseState {
    return RecoverSeedPhraseState()
  }
  
  override func reduce(state: RecoverSeedPhraseState, action: RecoverSeedPhraseAction) -> RecoverSeedPhraseState {
    var state = state
    
    switch action {
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
