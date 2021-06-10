import Foundation
import RxSwift

struct VerificationUserData {
  let userId: String
  let scanData: Data
  let idNumber: String
  let firstName: String
  let lastName: String
  let address: String
  let country: String
  let province: String
  let city: String
  let zipCode: String
  let scanFileName: String
  
  var tierId: String { return "1" }
}

enum VerificationAction: Equatable {
  case updateUserId(String)
  case updateSelectedImage(UIImage?)
  case updateIDNumber(String?)
  case updateFirstName(String?)
  case updateLastName(String?)
  case updateAddress(String?)
  case updateCountry(String?)
  case updateProvince(String?)
  case updateCity(String?)
  case updateZipCode(String?)
  case updateImageError(String?)
  case updateIDNumberError(String?)
  case updateFirstNameError(String?)
  case updateLastNameError(String?)
  case updateAddressError(String?)
  case updateCountryError(String?)
  case updateProvinceError(String?)
  case updateCityError(String?)
  case updateZipCodeError(String?)
  case updateValidationState
}

struct VerificationState: Equatable {
  var userId: String = ""
  var selectedImage: UIImage?
  var idNumber: String = ""
  var firstName: String = ""
  var lastName: String = ""
  var address: String = ""
  var country: String = ""
  var province: String = ""
  var city: String = ""
  var zipCode: String = ""
  var imageError: String?
  var idNumberError: String?
  var firstNameError: String?
  var lastNameError: String?
  var addressError: String?
  var countryError: String?
  var provinceError: String?
  var cityError: String?
  var zipCodeError: String?
  var validationState: ValidationState = .unknown
  
  var selectedImageData: Data? {
    return selectedImage?.jpegData(compressionQuality: 0.75)
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
  
  struct Constants {
    static let maxIdNumberCharacters = 9
    static let maxZipCodeCharacters = 5
  }
  
  override var initialState: VerificationState {
    return VerificationState()
  }
  
  override func reduce(state: VerificationState, action: VerificationAction) -> VerificationState {
    var state = state
    
    switch action {
    case .updateUserId(let userId):
        state.userId = userId
    case let .updateSelectedImage(image):
      state.selectedImage = image
      state.imageError = nil
    case let .updateIDNumber(idNumber):
      if let idNumber = idNumber?.prefix(Constants.maxIdNumberCharacters) {
        state.idNumber = String(idNumber)
      } else {
        state.idNumber = ""
      }
      state.idNumberError = nil
    case let .updateFirstName(firstName):
      state.firstName = firstName ?? ""
      state.firstNameError = nil
    case let .updateLastName(lastName):
      state.lastName = lastName ?? ""
      state.lastNameError = nil
    case let .updateAddress(address):
      state.address = address ?? ""
      state.addressError = nil
    case let .updateCountry(country):
      state.country = country ?? ""
      state.province = ""
      state.city = ""
      state.countryError = nil
      state.provinceError = nil
      state.cityError = nil
    case let .updateProvince(province):
      state.province = province ?? ""
      state.city = ""
      state.provinceError = nil
      state.cityError = nil
    case let .updateCity(city):
      state.city = city ?? ""
      state.cityError = nil
    case let .updateZipCode(zipCode):
      if let zipCode = zipCode?.prefix(Constants.maxZipCodeCharacters) {
        state.zipCode = String(zipCode)
      } else {
        state.zipCode = ""
      }
      state.zipCodeError = nil
    case let .updateImageError(imageError): state.imageError = imageError
    case let .updateIDNumberError(idNumberError): state.idNumberError = idNumberError
    case let .updateFirstNameError(firstNameError): state.firstNameError = firstNameError
    case let .updateLastNameError(lastNameError): state.lastNameError = lastNameError
    case let .updateAddressError(addressError): state.addressError = addressError
    case let .updateCountryError(countryError): state.countryError = countryError
    case let .updateProvinceError(provinceError): state.provinceError = provinceError
    case let .updateCityError(cityError): state.cityError = cityError
    case let .updateZipCodeError(zipCodeError): state.zipCodeError = zipCodeError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout VerificationState) {
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
    
    if state.idNumber.count != 9 || state.idNumber.rangeOfCharacter(from: CharacterSet.alphanumerics.inverted) != nil {
      let errorString = localize(L.Verification.Form.Error.notValidIdNumber)
      state.validationState = .invalid(errorString)
      state.idNumberError = errorString
    } else {
      state.idNumberError = nil
    }
    
    if state.firstName.count == 0 || state.firstName.count > 255 {
      let errorString = localize(L.Verification.Form.Error.notValidFirstName)
      state.validationState = .invalid(errorString)
      state.firstNameError = errorString
    } else {
      state.firstNameError = nil
    }
    
    if state.lastName.count == 0 || state.lastName.count > 255 {
      let errorString = localize(L.Verification.Form.Error.notValidLastName)
      state.validationState = .invalid(errorString)
      state.lastNameError = errorString
    } else {
      state.lastNameError = nil
    }
    
    if state.address.count == 0 || state.address.count > 255 {
      let errorString = localize(L.Verification.Form.Error.notValidAddress)
      state.validationState = .invalid(errorString)
      state.addressError = errorString
    } else {
      state.addressError = nil
    }
    
    if state.country.count == 0 {
      let errorString = localize(L.Verification.Form.Error.countryRequired)
      state.validationState = .invalid(errorString)
      state.countryError = errorString
    } else {
      state.countryError = nil
    }
    
    if state.province.count == 0 {
      let errorString = localize(L.Verification.Form.Error.provinceRequired)
      state.validationState = .invalid(errorString)
      state.provinceError = errorString
    } else {
      state.provinceError = nil
    }
    
    if state.city.count == 0 {
      let errorString = localize(L.Verification.Form.Error.cityRequired)
      state.validationState = .invalid(errorString)
      state.cityError = errorString
    } else {
      state.cityError = nil
    }
    
    if state.zipCode.count != 5 || state.zipCode.rangeOfCharacter(from: CharacterSet.decimalDigits.inverted) != nil {
      let errorString = localize(L.Verification.Form.Error.notValidZipCode)
      state.validationState = .invalid(errorString)
      state.zipCodeError = errorString
    } else {
      state.zipCodeError = nil
    }
  }
}
