import UIKit
import RxSwift
import RxCocoa

class VerificationFormView: UIView, UIPickerViewDelegate, UIPickerViewDataSource {
  
  let didSelectPickerRow = PublishRelay<String>()
  
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
  
  let countriesPickerView: UIPickerView = {
    let picker = UIPickerView()
    picker.isHidden = true
    return picker
  }()
  
  let provinceTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .province)
    return textField
  }()
  
  let provincesPickerView: UIPickerView = {
    let picker = UIPickerView()
    picker.isHidden = true
    return picker
  }()
  
  let cityTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .city)
    return textField
  }()
  
  let citiesPickerView: UIPickerView = {
    let picker = UIPickerView()
    picker.isHidden = true
    return picker
  }()
  
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
  
  let countryTapRecognizer = UITapGestureRecognizer()
  let provinceTapRecognizer = UITapGestureRecognizer()
  let cityTapRecognizer = UITapGestureRecognizer()
  
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
                                  countryTextField,
                                  countriesPickerView,
                                  provinceTextField,
                                  provincesPickerView,
                                  cityTextField,
                                  citiesPickerView,
                                  zipCodeTextField,
                                  addressTextField,
                                  sendButton)
    countryTextField.addGestureRecognizer(countryTapRecognizer)
    provinceTextField.addGestureRecognizer(provinceTapRecognizer)
    cityTextField.addGestureRecognizer(cityTapRecognizer)
    
    countriesPickerView.delegate = self
    countriesPickerView.dataSource = self
    provincesPickerView.delegate = self
    provincesPickerView.dataSource = self
    citiesPickerView.delegate = self
    citiesPickerView.dataSource = self
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
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
    if pickerView === countriesPickerView { return didSelectPickerRow.accept(countries[row]) }
    if pickerView === provincesPickerView { return didSelectPickerRow.accept(provinces[row]) }
    if pickerView === citiesPickerView { return didSelectPickerRow.accept(cities[row]) }
  }
}

extension Reactive where Base == VerificationFormView {
  var countryTap: Driver<Void> {
    return base.countryTapRecognizer.rx.event.asDriver().map { _ in }
  }
  var provinceTap: Driver<Void> {
    return base.provinceTapRecognizer.rx.event.asDriver().map { _ in }
  }
  var cityTap: Driver<Void> {
    return base.cityTapRecognizer.rx.event.asDriver().map { _ in }
  }
  var selectPickerItem: Driver<String> {
    return base.didSelectPickerRow.asDriver(onErrorJustReturn: "")
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
