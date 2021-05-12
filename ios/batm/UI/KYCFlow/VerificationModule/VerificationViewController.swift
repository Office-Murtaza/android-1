import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class VerificationViewController: ModuleViewController<VerificationPresenter> {
    private lazy var rootScrollView = RootScrollView()
    private lazy var filePickerView = VerificationFilePickerView()
    private lazy var formView = VerificationFormView()
    private lazy var sendButton = MDCButton.send
    
    override func setupUI() {
        title = localize(L.Verification.title)
        
        view.addSubviews(rootScrollView)
        
        rootScrollView.contentInsetAdjustmentBehavior = .never
        rootScrollView.contentView.addSubviews(filePickerView,
                                               formView,
                                               sendButton)
        filePickerView.setImageContainer(with: localize(L.KYC.Button.addID))
        setupDefaultKeyboardHandling()
    }
    
    override func setupLayout() {
        rootScrollView.snp.makeConstraints {
            $0.top.equalTo(view.safeAreaLayoutGuide)
            $0.left.right.bottom.equalToSuperview()
        }
        rootScrollView.contentView.snp.makeConstraints {
            $0.height.greaterThanOrEqualToSuperview()
        }
        
        filePickerView.snp.makeConstraints {
            $0.top.equalToSuperview().offset(24)
            $0.left.right.equalToSuperview().inset(15)
        }
        formView.snp.makeConstraints {
            $0.top.equalTo(filePickerView.snp.bottom).offset(15)
            $0.left.right.equalToSuperview().inset(15)
            $0.bottom.lessThanOrEqualTo(sendButton.snp.top).offset(-5)
        }
        sendButton.snp.makeConstraints {
            $0.height.equalTo(50)
            $0.left.right.equalToSuperview().inset(15)
            $0.bottom.equalToSuperview().offset(-40)
        }
    }
    
    func setupUIBindings() {
        rx.firstTimeViewDidAppear
            .asObservable()
            .doOnNext { [weak self] in
                self?.presenter.didViewLoad.accept(())
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.selectedImage }
            .bind(to: filePickerView.rx.image)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.imageError }
            .bind(to: filePickerView.rx.imageErrorText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.idNumber }
            .bind(to: formView.rx.idNumberText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.firstName }
            .bind(to: formView.rx.firstNameText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.lastName }
            .bind(to: formView.rx.lastNameText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.address }
            .bind(to: formView.rx.addressText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.idNumber }
            .bind(to: formView.rx.idNumberText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.zipCode }
            .bind(to: formView.rx.zipCodeText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.country }
            .bind(to: formView.rx.countryText)
            .disposed(by: disposeBag)
        
        let emptyCountryDriver = presenter.state
            .asObservable()
            .map { $0.country.count > 0 }
        
        emptyCountryDriver
            .bind(to: formView.fakeProvinceTextField.rx.isEnabled)
            .disposed(by: disposeBag)
        
        emptyCountryDriver
            .bind(to: formView.provinceTextField.rx.isEnabled)
            .disposed(by: disposeBag)
        
        let emptyProvinceDriver = presenter.state
            .asObservable()
            .map { $0.province.count > 0 }
        
        emptyProvinceDriver
            .bind(to: formView.fakeCityTextField.rx.isEnabled)
            .disposed(by: disposeBag)
        
        emptyProvinceDriver
            .bind(to: formView.cityTextField.rx.isEnabled)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.province }
            .bind(to: formView.rx.provinceText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.city }
            .bind(to: formView.rx.cityText)
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
            .asObservable()
            .map { $0.idNumberError }
            .bind(to: formView.rx.idNumberErrorText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.firstNameError }
            .bind(to: formView.rx.firstNameErrorText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.lastNameError }
            .bind(to: formView.rx.lastNameErrorText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.addressError }
            .bind(to: formView.rx.addressErrorText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.countryError }
            .bind(to: formView.rx.countryErrorText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.provinceError }
            .bind(to: formView.rx.provinceErrorText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.cityError }
            .bind(to: formView.rx.cityErrorText)
            .disposed(by: disposeBag)
        
        presenter.state
            .asObservable()
            .map { $0.zipCodeError }
            .bind(to: formView.rx.zipCodeErrorText)
            .disposed(by: disposeBag)
        
        Driver.merge(filePickerView.rx.select,
                     filePickerView.rx.remove,
                     sendButton.rx.tap.asDriver())
            .drive(onNext: { [unowned self] in self.view.endEditing(true) })
            .disposed(by: disposeBag)
    }
    
    override func setupBindings() {
        setupUIBindings()
        
        let selectDriver = filePickerView.rx.select
        let removeDriver = filePickerView.rx.remove
        let updateIDNumberDriver = formView.rx.idNumberText.asDriver()
        let updateFirstNameDriver = formView.rx.firstNameText.asDriver()
        let updateLastNameDriver = formView.rx.lastNameText.asDriver()
        let updateAddressDriver = formView.rx.addressText.asDriver()
        let updateZipCodeDriver = formView.rx.zipCodeText.asDriver()
        let selectCountryDriver = formView.rx.selectCountry
        let selectProvinceDriver = formView.rx.selectProvince
        let selectCityDriver = formView.rx.selectCity
        let sendDriver = sendButton.rx.tap.asDriver()
        
        presenter.bind(input: VerificationPresenter.Input(select: selectDriver,
                                                          remove: removeDriver,
                                                          updateIDNumber: updateIDNumberDriver,
                                                          updateFirstName: updateFirstNameDriver,
                                                          updateLastName: updateLastNameDriver,
                                                          updateAddress: updateAddressDriver,
                                                          selectCountry: selectCountryDriver,
                                                          selectProvince: selectProvinceDriver,
                                                          selectCity: selectCityDriver,
                                                          updateZipCode: updateZipCodeDriver,
                                                          send: sendDriver))
    }
}
