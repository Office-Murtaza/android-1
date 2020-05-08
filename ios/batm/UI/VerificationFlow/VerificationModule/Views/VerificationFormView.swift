import UIKit
import RxSwift
import RxCocoa

class VerificationFormView: UIView, UIPickerViewDelegate, UIPickerViewDataSource {
  
  let didSelectCountry = PublishRelay<String>()
  let didSelectProvince = PublishRelay<String>()
  let didSelectCity = PublishRelay<String>()
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 15
    return stackView
  }()
  
  let idNumberTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .idNumber)
    return textField
  }()
  
  let firstNameTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .firstName)
    return textField
  }()
  
  let lastNameTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .lastName)
    return textField
  }()
  
  let countryTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .country)
    return textField
  }()
  
  let provinceTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .province)
    return textField
  }()
  
  let cityTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .city)
    return textField
  }()
  
  let countryTextFieldContainer = UIView()
  let provinceTextFieldContainer = UIView()
  let cityTextFieldContainer = UIView()
  
  let fakeCountryTextField = FakeTextField()
  let fakeProvinceTextField = FakeTextField()
  let fakeCityTextField = FakeTextField()
  
  let countriesPickerView = UIPickerView()
  let provincesPickerView = UIPickerView()
  let citiesPickerView = UIPickerView()
  
  let zipCodeTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .zipCode)
    return textField
  }()
  
  let addressTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .address)
    return textField
  }()
  
  let sendButton: MainButton = {
    let button = MainButton()
    button.configure(for: .send)
    return button
  }()
  
  var typeableFields: [MainTextField] {
    return [
      idNumberTextField,
      firstNameTextField,
      lastNameTextField,
      zipCodeTextField,
      addressTextField
    ]
  }
  
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
                                  countryTextFieldContainer,
                                  provinceTextFieldContainer,
                                  cityTextFieldContainer,
                                  zipCodeTextField,
                                  addressTextField,
                                  sendButton)
    
    countryTextFieldContainer.addSubviews(countryTextField,
                                          fakeCountryTextField)
    
    provinceTextFieldContainer.addSubviews(provinceTextField,
                                           fakeProvinceTextField)
    
    cityTextFieldContainer.addSubviews(cityTextField,
                                       fakeCityTextField)
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
}
