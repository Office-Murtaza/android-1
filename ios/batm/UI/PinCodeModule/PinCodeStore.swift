import Foundation
import RxSwift
import PhoneNumberKit

enum PinCodeStage {
  case setup
  case verification
}

enum PinCodeAction: Equatable {
  case updateCode(String?)
  case updateStage(PinCodeStage)
}

struct PinCodeState: Equatable {
  var code = ""
  var stage: PinCodeStage = .setup
  
  var title: String {
    switch stage {
    case .setup: return localize(L.PinCode.Setup.title)
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
    case let .updateCode(code): state.code = code ?? ""
    case let .updateStage(stage): state.stage = stage
    }
    
    return state
  }
}
