import Foundation
import RxSwift

struct VIPVerificationUserData {
  let userId: Int
  let selfieData: Data
  let ssn: String
  let selfieFileName: String
  
  var tierId: String { return "2" }
}

enum VIPVerificationAction: Equatable {
  case getUserId(Int?)
  case updateSelectedImage(UIImage?)
  case updateSSN(String?)
  case updateImageError(String?)
  case updateSSNError(String?)
  case updateValidationState
}

struct VIPVerificationState: Equatable {
  var userId: Int?
  var selectedImage: UIImage?
  var ssn: String = ""
  var imageError: String?
  var ssnError: String?
  var validationState: ValidationState = .unknown
  
  var selectedImageData: Data? {
    return selectedImage?.jpegData(compressionQuality: 0.75)
  }
}

final class VIPVerificationStore: ViewStore<VIPVerificationAction, VIPVerificationState> {
  
  struct Constants {
    static let maxSSNCharacters = 9
  }
  
  override var initialState: VIPVerificationState {
    return VIPVerificationState()
  }
  
  override func reduce(state: VIPVerificationState, action: VIPVerificationAction) -> VIPVerificationState {
    var state = state
    
    switch action {
    case .getUserId(let userId):
        state.userId = userId
    case let .updateSelectedImage(image):
      state.selectedImage = image
      state.imageError = nil
    case let .updateSSN(ssn):
      if let ssn = ssn?.prefix(Constants.maxSSNCharacters) {
        state.ssn = String(ssn)
      } else {
        state.ssn = ""
      }
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
