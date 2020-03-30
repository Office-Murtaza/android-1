import Foundation
import RxSwift

struct VerificationUserData {
  let scanData: Data
  let idNumber: String
  let firstName: String
  let lastName: String
  let address: String
  let country: String
  let province: String
  let city: String
  let zipCode: String
  
  var tierId: String { return "1" }
}

enum VerificationPickerOption {
  case countries
  case provinces
  case cities
  case none
}

enum VerificationAction: Equatable {
  case updateSelectedImage(UIImage?)
  case updateIDNumber(String?)
  case updateFirstName(String?)
  case updateLastName(String?)
  case updateAddress(String?)
  case updateCountry(String?)
  case updateProvince(String?)
  case updateCity(String?)
  case updateZipCode(String?)
  case updatePickerOption(VerificationPickerOption)
  case updateValidationState
  case makeInvalidState(String)
}

struct VerificationState: Equatable {
  var selectedImage: UIImage?
  var idNumber: String = ""
  var firstName: String = ""
  var lastName: String = ""
  var address: String = ""
  var country: String = ""
  var province: String = ""
  var city: String = ""
  var zipCode: String = ""
  var pickerOption: VerificationPickerOption = .none
  var validationState: ValidationState = .unknown
  
  var selectedImageData: Data? {
    return selectedImage?.pngData()
  }
  
  var displayedCountries: [String] {
    var countries = VerificationConstants.countries.map { $0.name }
    countries.insert("", at: 0)
    return countries
  }
  
  var displayedProvinces: [String] {
    var provinces = VerificationConstants.countries
      .first { $0.name == country }?.states
      .map { $0.name } ?? []
    provinces.insert("", at: 0)
    return provinces
  }
  
  var displayedCities: [String] {
    var cities = VerificationConstants.countries
      .first { $0.name == country }?.states
      .first { $0.name == province }?.cities ?? []
    cities.insert("", at: 0)
    return cities
  }
}

final class VerificationStore: ViewStore<VerificationAction, VerificationState> {
  
  override var initialState: VerificationState {
    return VerificationState()
  }
  
  override func reduce(state: VerificationState, action: VerificationAction) -> VerificationState {
    var state = state
    
    switch action {
    case let .updateSelectedImage(image): state.selectedImage = image
    case let .updateIDNumber(idNumber): state.idNumber = idNumber ?? ""
    case let .updateFirstName(firstName): state.firstName = firstName ?? ""
    case let .updateLastName(lastName): state.lastName = lastName ?? ""
    case let .updateAddress(address): state.address = address ?? ""
    case let .updateCountry(country):
      state.country = country ?? ""
      state.province = ""
      state.city = ""
    case let .updateProvince(province):
      state.province = province ?? ""
      state.city = ""
    case let .updateCity(city): state.city = city ?? ""
    case let .updateZipCode(zipCode): state.zipCode = zipCode ?? ""
    case let .updatePickerOption(pickerOption): state.pickerOption = pickerOption
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: VerificationState) -> ValidationState {
    guard state.idNumber.isNotEmpty &&
      state.firstName.isNotEmpty &&
      state.lastName.isNotEmpty &&
      state.address.isNotEmpty &&
      state.country.isNotEmpty &&
      state.province.isNotEmpty &&
      state.city.isNotEmpty &&
      state.zipCode.isNotEmpty
      else {
        return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.selectedImage != nil else {
      return .invalid(localize(L.Verification.Form.Error.idScanRequired))
    }
    
    guard state.selectedImageData != nil else {
      return .invalid(localize(L.Verification.Form.Error.imageBroken))
    }
    
    return .valid
  }
}
