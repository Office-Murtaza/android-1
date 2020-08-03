import UIKit
import RxSwift
import RxCocoa
import SnapKit
import MaterialComponents

class RecoverViewController: ModuleViewController<RecoverPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let errorView = ErrorView()
  
  let formView = RecoverFormView()
  
  let nextButton = MDCButton.next
  
  override var shouldShowNavigationBar: Bool { return true }
  
  override func setupUI() {
    title = localize(L.Recover.title)
    
    view.addSubviews(rootScrollView)
    
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
      .map { $0.phoneE164.count > 0 && $0.isAllFieldsNotEmpty }
      .bind(to: nextButton.rx.isEnabled)
      .disposed(by: disposeBag)
      
    presenter.state
      .map { $0.validationState }
      .mapToErrorMessage()
      .drive(onNext: { [errorView] in
        errorView.isHidden = $0 == nil
        errorView.configure(for: $0)
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
    let nextDriver = nextButton.rx.tap.asDriver()
    presenter.bind(input: RecoverPresenter.Input(updatePhoneNumber: updatePhoneNumberDriver,
                                                 updatePassword: updatePasswordDriver,
                                                 next: nextDriver))
  }
}
