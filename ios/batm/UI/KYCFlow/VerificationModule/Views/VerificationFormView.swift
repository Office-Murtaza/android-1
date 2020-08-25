import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class VerificationFormView: UIView, UIPickerViewDelegate, UIPickerViewDataSource {
  
  let didSelectCountry = PublishRelay<String>()
  let didSelectProvince = PublishRelay<String>()
  let didSelectCity = PublishRelay<String>()
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let idNumberTextField = MDCTextField.idNumber
  let firstNameTextField = MDCTextField.default
  let lastNameTextField = MDCTextField.default
  let addressTextField = MDCTextField.default
  let countryTextField = MDCTextField.dropdown
  let provinceTextField = MDCTextField.dropdown
  let cityTextField = MDCTextField.dropdown
  let zipCodeTextField = MDCTextField.zipCode
  
  let idNumberTextFieldController: ThemedTextInputControllerOutlined
  let firstNameTextFieldController: ThemedTextInputControllerOutlined
  let lastNameTextFieldController: ThemedTextInputControllerOutlined
  let addressTextFieldController: ThemedTextInputControllerOutlined
  let countryTextFieldController: ThemedTextInputControllerOutlined
  let provinceTextFieldController: ThemedTextInputControllerOutlined
  let cityTextFieldController: ThemedTextInputControllerOutlined
  let zipCodeTextFieldController: ThemedTextInputControllerOutlined
  
  let countryTextFieldContainer = UIView()
  let provinceTextFieldContainer = UIView()
  let cityTextFieldContainer = UIView()
  
  let fakeCountryTextField = FakeTextField()
  let fakeProvinceTextField = FakeTextField()
  let fakeCityTextField = FakeTextField()
  
  let countriesPickerView = UIPickerView()
  let provincesPickerView = UIPickerView()
  let citiesPickerView = UIPickerView()
  
  var countries: [String] = [] {
    didSet {
      countriesPickerView.reloadAllComponents()
      countriesPickerView.selectRow(0, inComponent: 0, animated: false)
    }
  }
  
  var provinces: [String] = [] {
    didSet {
      provincesPickerView.reloadAllComponents()
      provincesPickerView.selectRow(0, inComponent: 0, animated: false)
    }
  }
  
  var cities: [String] = [] {
    didSet {
      citiesPickerView.reloadAllComponents()
      citiesPickerView.selectRow(0, inComponent: 0, animated: false)
    }
  }
  
  override init(frame: CGRect) {
    idNumberTextFieldController = ThemedTextInputControllerOutlined(textInput: idNumberTextField)
    firstNameTextFieldController = ThemedTextInputControllerOutlined(textInput: firstNameTextField)
    lastNameTextFieldController = ThemedTextInputControllerOutlined(textInput: lastNameTextField)
    addressTextFieldController = ThemedTextInputControllerOutlined(textInput: addressTextField)
    countryTextFieldController = ThemedTextInputControllerOutlined(textInput: countryTextField)
    provinceTextFieldController = ThemedTextInputControllerOutlined(textInput: provinceTextField)
    cityTextFieldController = ThemedTextInputControllerOutlined(textInput: cityTextField)
    zipCodeTextFieldController = ThemedTextInputControllerOutlined(textInput: zipCodeTextField)
    
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubview(stackView)
    stackView.addArrangedSubviews(idNumberTextField,
                                  firstNameTextField,
                                  lastNameTextField,
                                  addressTextField,
                                  countryTextFieldContainer,
                                  provinceTextFieldContainer,
                                  cityTextFieldContainer,
                                  zipCodeTextField)
    
    countryTextFieldContainer.addSubviews(countryTextField,
                                          fakeCountryTextField)
    
    provinceTextFieldContainer.addSubviews(provinceTextField,
                                           fakeProvinceTextField)
    
    cityTextFieldContainer.addSubviews(cityTextField,
                                       fakeCityTextField)
    
    idNumberTextFieldController.placeholderText = localize(L.Verification.Form.IDNumber.placeholder)
    firstNameTextFieldController.placeholderText = localize(L.Verification.Form.FirstName.placeholder)
    lastNameTextFieldController.placeholderText = localize(L.Verification.Form.LastName.placeholder)
    addressTextFieldController.placeholderText = localize(L.Verification.Form.Address.placeholder)
    countryTextFieldController.placeholderText = localize(L.Verification.Form.Country.placeholder)
    provinceTextFieldController.placeholderText = localize(L.Verification.Form.Province.placeholder)
    cityTextFieldController.placeholderText = localize(L.Verification.Form.City.placeholder)
    zipCodeTextFieldController.placeholderText = localize(L.Verification.Form.ZipCode.placeholder)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    [countryTextField, fakeCountryTextField].forEach {
      $0.snp.makeConstraints {
        $0.edges.equalToSuperview()
      }
    }
    [provinceTextField, fakeProvinceTextField].forEach {
      $0.snp.makeConstraints {
        $0.edges.equalToSuperview()
      }
    }
    [cityTextField, fakeCityTextField].forEach {
      $0.snp.makeConstraints {
        $0.edges.equalToSuperview()
      }
    }
    
    setupPickers()
  }
  
  private func setupPickers() {
    fakeCountryTextField.inputView = countriesPickerView
    fakeProvinceTextField.inputView = provincesPickerView
    fakeCityTextField.inputView = citiesPickerView
    
    countriesPickerView.delegate = self
    countriesPickerView.dataSource = self
    provincesPickerView.delegate = self
    provincesPickerView.dataSource = self
    citiesPickerView.delegate = self
    citiesPickerView.dataSource = self
  }
  
  func numberOfComponents(in pickerView: UIPickerView) -> Int {
    return 1
  }
  
  func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
    if pickerView === countriesPickerView { return countries.count }
    if pickerView === provincesPickerView { return provinces.count }
    if pickerView === citiesPickerView { return cities.count }
    return 0
  }
  
  func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
    if pickerView === countriesPickerView { return countries[row] }
    if pickerView === provincesPickerView { return provinces[row] }
    if pickerView === citiesPickerView { return cities[row] }
    return nil
  }
  
  func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
    if pickerView === countriesPickerView { return didSelectCountry.accept(countries[row]) }
    if pickerView === provincesPickerView { return didSelectProvince.accept(provinces[row]) }
    if pickerView === citiesPickerView { return didSelectCity.accept(cities[row]) }
  }
}

extension Reactive where Base == VerificationFormView {
  var selectCountry: Driver<String> {
    return base.didSelectCountry.asDriver(onErrorJustReturn: "")
  }
  var selectProvince: Driver<String> {
    return base.didSelectProvince.asDriver(onErrorJustReturn: "")
  }
  var selectCity: Driver<String> {
    return base.didSelectCity.asDriver(onErrorJustReturn: "")
  }
  var countries: Binder<[String]> {
    return Binder(base) { target, value in
      target.countries = value
    }
  }
  var provinces: Binder<[String]> {
    return Binder(base) { target, value in
      target.provinces = value
    }
  }
  var cities: Binder<[String]> {
    return Binder(base) { target, value in
      target.cities = value
    }
  }
  var idNumberText: ControlProperty<String?> {
    return base.idNumberTextField.rx.text
  }
  var firstNameText: ControlProperty<String?> {
    return base.firstNameTextField.rx.text
  }
  var lastNameText: ControlProperty<String?> {
    return base.lastNameTextField.rx.text
  }
  var addressText: ControlProperty<String?> {
    return base.addressTextField.rx.text
  }
  var countryText: ControlProperty<String?> {
    return base.countryTextField.rx.text
  }
  var provinceText: ControlProperty<String?> {
    return base.provinceTextField.rx.text
  }
  var cityText: ControlProperty<String?> {
    return base.cityTextField.rx.text
  }
  var zipCodeText: ControlProperty<String?> {
    return base.zipCodeTextField.rx.text
  }
  var idNumberErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.idNumberTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var firstNameErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.firstNameTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var lastNameErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.lastNameTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var addressErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.addressTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var countryErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.countryTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var provinceErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.provinceTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var cityErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.cityTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var zipCodeErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.zipCodeTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
}
