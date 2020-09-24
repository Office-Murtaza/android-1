import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents

class RecoverViewController: ModuleViewController<RecoverPresenter> {
  
  let didDisappearRelay = PublishRelay<Void>()
  
  let rootScrollView = RootScrollView()
  
  let formView = RecoverFormView()
  
  let nextButton = MDCButton.next
  
  override func viewDidDisappear(_ animated: Bool) {
    super.viewDidDisappear(animated)
    
    didDisappearRelay.accept(())
  }
  
  override func setupUI() {
    title = localize(L.Recover.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(formView,
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
      .map { $0.phoneE164.count > 0 && $0.isAllFieldsNotEmpty }
      .bind(to: nextButton.rx.isEnabled)
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
    let nextDriver = nextButton.rx.tap.asDriver()
    presenter.bind(input: RecoverPresenter.Input(didDisappear: didDisappearDriver,
                                                 updatePhoneNumber: updatePhoneNumberDriver,
                                                 updatePassword: updatePasswordDriver,
                                                 next: nextDriver))
  }
}
