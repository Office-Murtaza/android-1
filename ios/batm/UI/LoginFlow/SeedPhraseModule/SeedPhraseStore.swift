import Foundation

enum SeedPhraseAction: Equatable {
  case setupPhoneNumber(String)
  case setupPassword(String)
  case setupSeedPhrase(String)
  case updateValidationState
  case makeInvalidState(String)
}

struct SeedPhraseState: Equatable {
  
  var phoneNumber: String = ""
  var password: String = ""
  var seedPhrase: String = ""
  var validationState: ValidationState = .unknown
  
}

final class SeedPhraseStore: ViewStore<SeedPhraseAction, SeedPhraseState> {
  
  override var initialState: SeedPhraseState {
    return SeedPhraseState()
  }
  
  override func reduce(state: SeedPhraseState, action: SeedPhraseAction) -> SeedPhraseState {
    var state = state
    
    switch action {
    case let .setupPhoneNumber(phoneNumber): state.phoneNumber = phoneNumber
    case let .setupPassword(password): state.password = password
    case let .setupSeedPhrase(seedPhrase): state.seedPhrase = seedPhrase
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: SeedPhraseState) -> ValidationState {
    return .valid
  }
}
