import Foundation

enum SeedPhraseMode: Equatable {
  case creation(String, String)
  case showing
}

enum SeedPhraseAction: Equatable {
  case setupMode(SeedPhraseMode)
  case setupSeedPhrase([String])
  case updateValidationState
  case makeInvalidState(String)
  case pastePhrase([String])
  case updateSeedPhrase([String])
  case generateSeedPhrase([String])
  case resetSeedPhrase
}

struct SeedPhraseState: Equatable {
  
  var mode: SeedPhraseMode = .showing
  var seedPhrase: [String] = []
  var validationState: ValidationState = .unknown
    var generatedPhrase: [String] = []
    var fullSeedPhrase: String {
      return seedPhrase.joined(separator: " ")
    }
}

final class SeedPhraseStore: ViewStore<SeedPhraseAction, SeedPhraseState> {
  
  override var initialState: SeedPhraseState {
    return SeedPhraseState()
  }
  
  override func reduce(state: SeedPhraseState, action: SeedPhraseAction) -> SeedPhraseState {
    var state = state
    
    switch action {
    case let .setupMode(mode):
        state.mode = mode
    case let .setupSeedPhrase(seedPhrase):
        state.seedPhrase = seedPhrase
    case .updateValidationState:
        state.validationState = validate(&state)
    case let .makeInvalidState(error):
        state.validationState = .invalid(error)
    case let .pastePhrase(seedPhrase):
        state.seedPhrase = seedPhrase
        state.validationState = .valid
    case let .updateSeedPhrase(seedPhrase):
      state.seedPhrase = seedPhrase
      state.validationState = .valid
    case let .generateSeedPhrase(phrase):
        state.generatedPhrase = phrase
        state.validationState = .valid
    case .resetSeedPhrase:
        state.generatedPhrase = []
        state.seedPhrase = []
        state.validationState = .valid
    }
    
    
    return state
  }
  
    private func validate(_ state: inout SeedPhraseState) -> ValidationState {
        
        if state.seedPhrase.count != BTMWallet.seedPhraseLength {
            let errorString = localize(L.RecoverSeedPhrase.Form.Error.notValidLength)
            state.validationState = .invalid(errorString)
            return .invalid(errorString)
            
        }
        
        return .valid
    }
}
