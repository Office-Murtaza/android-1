import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents

class CreateWalletViewController: ModuleViewController<CreateWalletPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let errorView = ErrorView()
  
  let formView = CreateWalletFormView()
  
  let nextButton = MDCButton.next
  
  let backgroundDarkView: BackgroundDarkView = {
    let view = BackgroundDarkView()
    view.alpha = 0
    return view
  }()
  
  let codeView: CodeView = {
    let view = CodeView()
    view.alpha = 0
    return view
  }()
  
  override var shouldShowNavigationBar: Bool { return true }
  
  override func setupUI() {
    title = localize(L.CreateWallet.title)
    
    view.addSubviews(rootScrollView,
                     backgroundDarkView,
                     codeView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(errorView,
                                           formView,
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
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(5)
      $0.centerX.equalToSuperview()
    }
    formView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(20)
      $0.left.right.equalToSuperview().inset(15)
    }
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
    }
    backgroundDarkView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    codeView.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(30)
      $0.bottom.equalTo(view.safeAreaLayoutGuide).offset(-30)
    }
  }
  
  private func showCodeView() {
    UIView.animate(withDuration: 0.3) {
      self.backgroundDarkView.alpha = 1
      self.codeView.alpha = 1
    }
  }
  
  private func hideCodeView() {
    UIView.animate(withDuration: 0.3) {
      self.backgroundDarkView.alpha = 0
      self.codeView.alpha = 0
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
      .map { $0.code }
      .bind(to: codeView.smsCodeTextField.rx.text)
      .disposed(by: disposeBag)
    
    
    let errorMessageDriverObservable = presenter.state.asObservable()
      .map { $0.validationState }
      .mapToErrorMessage()
    let shouldShowCodePopupObservable = presenter.state.asObservable()
      .map { $0.shouldShowCodePopup }
    
    let combinedObservable = Observable.combineLatest(shouldShowCodePopupObservable,
                                                      errorMessageDriverObservable)
      
    combinedObservable
      .map { $0 ? nil : $1 }
      .subscribe(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
      })
      .disposed(by: disposeBag)
    
    combinedObservable
      .map { $0 ? $1 : nil }
      .bind(to: codeView.rx.error)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.shouldShowCodePopup }
      .distinctUntilChanged()
      .drive(onNext: { [unowned self] in
        if $0 {
          self.showCodeView()
        } else {
          self.hideCodeView()
        }
      })
      .disposed(by: disposeBag)
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }
  
  override func setupBindings() {
    setupUIBindings()
    
    let updatePhoneNumberDriver = formView.rx.phoneNumberText.asDriver()
    let updatePasswordDriver = formView.rx.passwordText.asDriver()
    let updateConfirmPasswordDriver = formView.rx.confirmPasswordText.asDriver()
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let nextDriver = nextButton.rx.tap.asDriver()
    let cancelCodeDriver = codeView.rx.cancelTap
    let confirmCodeDriver = codeView.rx.nextTap
    presenter.bind(input: CreateWalletPresenter.Input(updatePhoneNumber: updatePhoneNumberDriver,
                                                      updatePassword: updatePasswordDriver,
                                                      updateConfirmPassword: updateConfirmPasswordDriver,
                                                      updateCode: updateCodeDriver,
                                                      next: nextDriver,
                                                      cancelCode: cancelCodeDriver,
                                                      confirmCode: confirmCodeDriver))
  }
}
