import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents

class CreateWalletViewController: ModuleViewController<CreateWalletPresenter> {
  
  let didDisappearRelay = PublishRelay<Void>()
  
  let rootScrollView = RootScrollView()
  
  let formView = CreateWalletFormView()
  
  let termsAndConditionsView = TermsAndConditionsView()
  
  let nextButton = MDCButton.next
  
  override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    
    didDisappearRelay.accept(())
  }
  
  override func setupUI() {
    title = localize(L.CreateWallet.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(formView,
                                           termsAndConditionsView,
                                           nextButton)
    
    setupDefaultKeyboardHandling()
  }
  
  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.top.equalTo(view.safeAreaLayoutGuide)
      $0.left.right.bottom.equalToSuperview()
    }
    rootScrollView.contentView.snp.makeConstraints {
      $0.height.equalToSuperview()
    }
    formView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(20)
      $0.left.right.equalToSuperview().inset(15)
    }
    termsAndConditionsView.snp.makeConstraints {
      $0.left.equalToSuperview().inset(15)
      $0.bottom.equalTo(nextButton.snp.top).offset(-30)
    }
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  private func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.phoneNumber }
      .bind(to: formView.rx.phoneNumberText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.password }
      .bind(to: formView.rx.passwordText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.confirmPassword }
      .bind(to: formView.rx.confirmPasswordText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.phoneNumberError }
      .bind(to: formView.rx.phoneNumberErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.passwordError }
      .bind(to: formView.rx.passwordErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.confirmPasswordError }
      .bind(to: formView.rx.confirmPasswordErrorText)
      .disposed(by: disposeBag)
    
    Driver.combineLatest(termsAndConditionsView.rx.isAccepted,
                         presenter.state.map { $0.phoneE164.count > 0 && $0.isAllFieldsNotEmpty })
      .drive(onNext: { [unowned self] isAccepted, isStateValid in
        self.nextButton.isEnabled = isAccepted && isStateValid
      })
      .disposed(by: disposeBag)
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let didDisappearDriver = didDisappearRelay.asDriver(onErrorDriveWith: .empty())
    let updatePhoneNumberDriver = formView.rx.phoneNumberText.asDriver()
    let updatePasswordDriver = formView.rx.passwordText.asDriver()
    let updateConfirmPasswordDriver = formView.rx.confirmPasswordText.asDriver()
    let openTermsAndConditionsDriver = termsAndConditionsView.rx.termsAndConditionsTap
    let nextDriver = nextButton.rx.tap.asDriver()
    presenter.bind(input: CreateWalletPresenter.Input(didDisappear: didDisappearDriver,
                                                      updatePhoneNumber: updatePhoneNumberDriver,
                                                      updatePassword: updatePasswordDriver,
                                                      updateConfirmPassword: updateConfirmPasswordDriver,
                                                      openTermsAndConditions: openTermsAndConditionsDriver,
                                                      next: nextDriver))
  }
}
