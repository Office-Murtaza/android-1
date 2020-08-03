import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class PhoneVerificationViewController: ModuleViewController<PhoneVerificationPresenter> {
  
  let rootScrollView = RootScrollView()
  
  let errorView = ErrorView()
  
  let formView = PhoneVerificationFormView()
  
  let nextButton = MDCButton.next
  
  let resendCodeLabel = PhoneVerificationResendCodeLabel()
  
  override var shouldShowNavigationBar: Bool { return true }

  override func setupUI() {
    title = localize(L.PhoneVerification.title)
    
    view.addSubviews(rootScrollView)
    
    rootScrollView.contentInsetAdjustmentBehavior = .never
    rootScrollView.contentView.addSubviews(errorView,
                                           formView,
                                           nextButton,
                                           resendCodeLabel)
    
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
      $0.top.equalToSuperview().offset(40)
      $0.left.right.equalToSuperview().inset(40)
      $0.bottom.lessThanOrEqualTo(nextButton.snp.top).offset(-20)
      $0.bottom.lessThanOrEqualTo(view.safeAreaLayoutGuide).offset(-20)
    }
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalTo(resendCodeLabel.snp.top).offset(-25)
    }
    resendCodeLabel.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalToSuperview().offset(-40)
    }
  }
  
  private func triggerWrongCodeHapticFeedback() {
    let notificationFeedbackGenerator = UINotificationFeedbackGenerator()
    notificationFeedbackGenerator.prepare()
    notificationFeedbackGenerator.notificationOccurred(.error)
  }
  
  func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.isCodeFilled }
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
    
    presenter.didTypeWrongCode
      .asDriver(onErrorDriveWith: .empty())
      .drive(onNext: { [unowned self] in
        self.formView.codeTextField.shake()
        self.triggerWrongCodeHapticFeedback()
      })
      .disposed(by: disposeBag)
    
    resendCodeLabel.rx.tap
      .drive(onNext: { [view] in view?.makeToast(localize(L.PhoneVerification.codeSent)) })
      .disposed(by: disposeBag)
    
    nextButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let codeDriver = formView.rx.codeText
    let nextDriver = nextButton.rx.tap.asDriver()
    let resendCodeDriver = resendCodeLabel.rx.tap
    
    presenter.bind(input: PhoneVerificationPresenter.Input(code: codeDriver,
                                                           next: nextDriver,
                                                           resendCode: resendCodeDriver))
  }
}
