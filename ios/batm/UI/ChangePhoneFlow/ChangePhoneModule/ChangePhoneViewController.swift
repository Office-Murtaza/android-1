import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class ChangePhoneViewController: NavigationScreenViewController<ChangePhonePresenter> {
  
  let formView = ChangePhoneFormView()
  
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
  
  private var handler: KeyboardHandler!
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.addSubviews(backgroundDarkView,
                     codeView)
    
    customView.contentView.addSubview(formView)
    customView.setTitle(localize(L.ChangePhone.title))
    
    setupKeyboardHandling()
  }
  
  private func setupKeyboardHandling() {
    handler = KeyboardHandler(with: view)
    setupDefaultKeyboardHandling(with: handler)
  }

  override func setupLayout() {
    formView.snp.makeConstraints {
      $0.edges.equalToSuperview()
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
  
  func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.code }
      .bind(to: codeView.smsCodeTextField.rx.text)
      .disposed(by: disposeBag)
    
    
    let errorMessageDriverObservable = presenter.state.asObservable()
      .map { $0.validationState }
      .map { validationState -> String? in
        switch validationState {
        case .valid, .unknown: return nil
        case let .invalid(message): return message
        }
    }
    let shouldShowCodePopupObservable = presenter.state.asObservable()
      .map { $0.shouldShowCodePopup }
    
    let combinedObservable = Observable.combineLatest(shouldShowCodePopupObservable,
                                                      errorMessageDriverObservable)
    
    combinedObservable
      .map { $0 ? nil : $1 }
      .bind(to: formView.rx.error)
      .disposed(by: disposeBag)
    
    combinedObservable
      .map { $0 ? $1 : nil }
      .bind(to: codeView.rx.error)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.shouldShowCodePopup }
      .filter { $0 }
      .drive(onNext: { [unowned self] _ in self.showCodeView() })
      .disposed(by: disposeBag)
    
    backgroundDarkView.rx.tap
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updatePhoneNumberDriver = formView.phoneNumberTextField.rx.phoneNumber
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let cancelDriver = Driver.merge(codeView.rx.cancelTap,
                                    formView.rx.cancelTap)
    let changePhoneDriver = formView.rx.nextTap
    let confirmPhoneDriver = codeView.rx.nextTap
    
    presenter.bind(input: ChangePhonePresenter.Input(back: backDriver,
                                                     updatePhoneNumber: updatePhoneNumberDriver,
                                                     updateCode: updateCodeDriver,
                                                     cancel: cancelDriver,
                                                     changePhone: changePhoneDriver,
                                                     confirmCode: confirmPhoneDriver))
  }
}
