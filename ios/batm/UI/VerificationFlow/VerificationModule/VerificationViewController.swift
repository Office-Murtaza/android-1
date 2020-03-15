import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class VerificationViewController: NavigationScreenViewController<VerificationPresenter> {
  
  let errorView = ErrorView()
  
  let scanLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Verification.idScan)
    label.textColor = .slateGrey
    label.font = .poppinsSemibold16
    return label
  }()
  
  let filePickerView = VerificationFilePickerView()
  
  let formView = VerificationFormView()
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    customView.rootScrollView.canCancelContentTouches = false
    customView.setTitle(localize(L.Verification.title))
    customView.contentView.addSubviews(errorView,
                                       scanLabel,
                                       filePickerView,
                                       formView)
  }
  
  override func setupLayout() {
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(10)
      $0.centerX.equalToSuperview()
    }
    scanLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(35)
      $0.centerX.equalToSuperview()
    }
    filePickerView.snp.makeConstraints {
      $0.top.equalTo(scanLabel.snp.bottom).offset(15)
      $0.left.greaterThanOrEqualToSuperview().offset(30)
      $0.right.lessThanOrEqualToSuperview().offset(-30)
      $0.centerX.equalToSuperview()
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(filePickerView.snp.bottom).offset(35)
      $0.left.right.equalToSuperview().inset(30)
      $0.bottom.equalToSuperview().offset(-35)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.selectedImage }
      .bind(to: filePickerView.rx.image)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.pickerOption != .countries }
      .bind(to: formView.countriesPickerView.rx.isHidden)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.pickerOption != .provinces }
      .bind(to: formView.provincesPickerView.rx.isHidden)
      .disposed(by: disposeBag)
      
    presenter.state
      .asObservable()
      .map { $0.pickerOption != .cities }
      .bind(to: formView.citiesPickerView.rx.isHidden)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.country }
      .bind(to: formView.countryTextField.rx.text)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.province }
      .bind(to: formView.provinceTextField.rx.text)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.city }
      .bind(to: formView.cityTextField.rx.text)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.displayedCountries }
      .distinctUntilChanged()
      .bind(to: formView.rx.countries)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.displayedProvinces }
      .distinctUntilChanged()
      .bind(to: formView.rx.provinces)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.displayedCities }
      .distinctUntilChanged()
      .bind(to: formView.rx.cities)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let selectDriver = filePickerView.rx.select
    let removeDriver = filePickerView.rx.remove
    let updateIDNumberDriver = formView.idNumberTextField.rx.text.asDriver()
    let updateFirstNameDriver = formView.firstNameTextField.rx.text.asDriver()
    let updateLastNameDriver = formView.lastNameTextField.rx.text.asDriver()
    let updateAddressDriver = formView.addressTextField.rx.text.asDriver()
    let updateZipCodeDriver = formView.zipCodeTextField.rx.text.asDriver()
    let selectCountryDriver = formView.rx.countryTap
    let selectProvinceDriver = formView.rx.provinceTap
    let selectCityDriver = formView.rx.cityTap
    let updatePickerItemDriver = formView.rx.selectPickerItem
    let sendDriver = formView.sendButton.rx.tap.asDriver()
    
    presenter.bind(input: VerificationPresenter.Input(back: backDriver,
                                                      select: selectDriver,
                                                      remove: removeDriver,
                                                      updateIDNumber: updateIDNumberDriver,
                                                      updateFirstName: updateFirstNameDriver,
                                                      updateLastName: updateLastNameDriver,
                                                      updateAddress: updateAddressDriver,
                                                      updateZipCode: updateZipCodeDriver,
                                                      selectCountry: selectCountryDriver,
                                                      selectProvince: selectProvinceDriver,
                                                      selectCity: selectCityDriver,
                                                      updatePickerItem: updatePickerItemDriver,
                                                      send: sendDriver))
  }
}
