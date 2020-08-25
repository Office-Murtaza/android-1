import Foundation
import RxSwift

struct VIPVerificationUserData {
  let selfieData: Data
  let ssn: String
  
  var tierId: String { return "2" }
}

enum VIPVerificationAction: Equatable {
  case updateSelectedImage(UIImage?)
  case updateSSN(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct VIPVerificationState: Equatable {
  var selectedImage: UIImage?
  var ssn: String = ""
  var validationState: ValidationState = .unknown
  
  var selectedImageData: Data? {
    return selectedImage?.pngData()
  }
}

final class VIPVerificationStore: ViewStore<VIPVerificationAction, VIPVerificationState> {
  
  override var initialState: VIPVerificationState {
    return VIPVerificationState()
  }
  
  override func reduce(state: VIPVerificationState, action: VIPVerificationAction) -> VIPVerificationState {
    var state = state
    
    switch action {
    case let .updateSelectedImage(image): state.selectedImage = image
    case let .updateSSN(ssn): state.ssn = ssn ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: VIPVerificationState) -> ValidationState {
    guard state.ssn.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.selectedImage != nil else {
      return .invalid(localize(L.VIPVerification.Form.Error.idSelfieRequired))
    }
    
    guard state.selectedImageData != nil else {
      return .invalid(localize(L.Verification.Form.Error.imageBroken))
    }
    
    return .valid
  }
}
