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
  case updateImageError(String?)
  case updateSSNError(String?)
  case updateValidationState
}

struct VIPVerificationState: Equatable {
  var selectedImage: UIImage?
  var ssn: String = ""
  var imageError: String?
  var ssnError: String?
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
    case let .updateSelectedImage(image):
      state.selectedImage = image
      state.imageError = nil
    case let .updateSSN(ssn):
      state.ssn = ssn ?? ""
      state.ssnError = nil
    case let .updateImageError(imageError): state.imageError = imageError
    case let .updateSSNError(ssnError): state.ssnError = ssnError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout VIPVerificationState) {
    state.validationState = .valid
    
    if state.selectedImage == nil {
      let errorString = localize(L.Verification.Form.Error.idScanRequired)
      state.validationState = .invalid(errorString)
      state.imageError = errorString
    } else if state.selectedImageData == nil {
      let errorString = localize(L.Verification.Form.Error.imageBroken)
      state.validationState = .invalid(errorString)
      state.imageError = errorString
    } else {
      state.imageError = nil
    }
    
    if state.ssn.count != 9 || state.ssn.rangeOfCharacter(from: CharacterSet.decimalDigits.inverted) != nil {
      let errorString = localize(L.VIPVerification.Form.Error.notValidSSN)
      state.validationState = .invalid(errorString)
      state.ssnError = errorString
    } else {
      state.ssnError = nil
    }
  }
}
