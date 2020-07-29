import Foundation
import RxSwift

enum PinCodeStage {
  case setup
  case confirmation
  case verification
}

enum PinCodeAction: Equatable {
  case addDigit(String)
  case removeDigit
  case clearCode
  case updateStage(PinCodeStage)
  case setupCorrectCode(String)
}

struct PinCodeState: Equatable {
  var correctCode = ""
  var code = ""
  var stage: PinCodeStage = .setup
  
  var title: String {
    switch stage {
    case .setup: return localize(L.PinCode.Setup.title)
    case .confirmation: return localize(L.PinCode.Confirmation.title)
    case .verification: return localize(L.PinCode.Verification.title)
    }
  }
}

final class PinCodeStore: ViewStore<PinCodeAction, PinCodeState> {
  
  override var initialState: PinCodeState {
    return PinCodeState()
  }
  
  override func reduce(state: PinCodeState, action: PinCodeAction) -> PinCodeState {
    var state = state
    
    switch action {
    case let .addDigit(digit): state.code += digit
    case .removeDigit: _ = state.code.popLast()
    case .clearCode: state.code = ""
    case let .updateStage(stage): state.stage = stage
    case let .setupCorrectCode(correctCode): state.correctCode = correctCode
    }
    
    return state
  }
}
