import Foundation
import RxSwift

enum PinCodeType {
  case old
  case current
  case new
}

enum PinCodeStage {
  case setup
  case confirmation
  case verification
}

enum PinCodeAction: Equatable {
  case setupStage(PinCodeStage)
  case setupType(PinCodeType)
  case setupCorrectCode(String)
  case setupShouldShowNavBar(Bool)
  case addDigit(String)
  case removeDigit
  case clearCode
  case localAuthOnStartEnabled(Bool)
}

struct PinCodeState: Equatable {
  var stage: PinCodeStage = .setup
  var type: PinCodeType = .current
  var correctCode = ""
  var code = ""
  var shouldShowNavBar = false
  var isEnabledLocalAuthOnStart = false
  
  var title: String {
    let title: String
    
    switch stage {
    case .setup: title = localize(L.PinCode.Stage.Setup.title)
    case .confirmation: title = localize(L.PinCode.Stage.Confirmation.title)
    case .verification: title = localize(L.PinCode.Stage.Verification.title)
    }
    
    let typeString: String
    
    switch type {
    case .old: typeString = localize(L.PinCode.PinType.Old.title)
    case .current: typeString = localize(L.PinCode.PinType.Current.title)
    case .new: typeString = localize(L.PinCode.PinType.New.title)
    }
    
    return String(format: title, typeString)
  }
}

final class PinCodeStore: ViewStore<PinCodeAction, PinCodeState> {
  
  override var initialState: PinCodeState {
    return PinCodeState()
  }
  
  override func reduce(state: PinCodeState, action: PinCodeAction) -> PinCodeState {
    var state = state
    
    switch action {
    case let .setupStage(stage): state.stage = stage
    case let .setupType(type): state.type = type
    case let .setupCorrectCode(correctCode): state.correctCode = correctCode
    case let .setupShouldShowNavBar(shouldShowNavBar): state.shouldShowNavBar = shouldShowNavBar
    case let .addDigit(digit): state.code += digit
    case .removeDigit: _ = state.code.popLast()
    case .clearCode: state.code = ""
    case let .localAuthOnStartEnabled(isEnabled): state.isEnabledLocalAuthOnStart = isEnabled
    }
    
    return state
  }
}
