import UIKit
import RxCocoa
import RxSwift
import SnapKit

final class ChangePhoneViewController: ModuleViewController<ChangePhonePresenter> {
  
  let tapRecognizer = UITapGestureRecognizer()
  
  let rootScrollView: UIScrollView = {
    let scrollView = UIScrollView()
    scrollView.bounces = false
    scrollView.contentInsetAdjustmentBehavior = .never
    scrollView.keyboardDismissMode = .interactive
    return scrollView
  }()
  
  let contentView = UIView()
  
  let backgroundImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_background"))
    imageView.contentMode = .scaleAspectFill
    imageView.clipsToBounds = true
    return imageView
  }()
  
  let safeAreaContainer = UIView()
  
  let backButton: UIButton = {
    let button = UIButton()
    button.setImage(UIImage(named: "back"), for: .normal)
    return button
  }()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.ChangePhone.title)
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
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
  
  override var shouldShowNavigationBar: Bool {
    return false
  }
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  private func registerForKeyboardNotifications() {
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(adjustForKeyboard),
                                           name: UIResponder.keyboardWillShowNotification,
                                           object: nil)
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(adjustForKeyboard),
                                           name: UIResponder.keyboardWillHideNotification,
                                           object: nil)
  }
  
  @objc private func adjustForKeyboard(notification: Notification) {
    guard let keyboardValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue else { return }
    
    let keyboardHeight = keyboardValue.cgRectValue.size.height
    
    if notification.name == UIResponder.keyboardWillHideNotification {
      rootScrollView.contentInset = .zero
    } else {
      rootScrollView.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: keyboardHeight, right: 0)
    }
    
    rootScrollView.scrollIndicatorInsets = rootScrollView.contentInset
  }

  override func setupUI() {
    registerForKeyboardNotifications()
    
    view.backgroundColor = .white
    
    view.addSubview(rootScrollView)
    rootScrollView.addSubview(contentView)
    contentView.addSubviews(backgroundImageView,
                            safeAreaContainer,
                            formView,
                            backgroundDarkView,
                            codeView)
    contentView.addGestureRecognizer(tapRecognizer)
    safeAreaContainer.addSubviews(backButton,
                                  titleLabel)
  }

  override func setupLayout() {
    rootScrollView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    contentView.snp.makeConstraints {
      $0.edges.equalToSuperview()
      $0.size.equalToSuperview()
    }
    backgroundImageView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.bottom.equalTo(contentView.snp.top).offset(view.safeAreaInsets.top + 44)
    }
    safeAreaContainer.snp.makeConstraints {
      $0.left.right.bottom.equalTo(backgroundImageView)
      $0.top.equalToSuperview().offset(view.safeAreaInsets.top)
    }
    backButton.snp.makeConstraints {
      $0.centerY.equalTo(titleLabel)
      $0.left.equalToSuperview().offset(15)
      $0.size.equalTo(45)
    }
    titleLabel.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom)
      $0.left.right.equalToSuperview()
    }
    backgroundDarkView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    codeView.snp.makeConstraints {
      $0.bottom.equalToSuperview().offset(-view.safeAreaInsets.bottom - 60)
      $0.left.right.equalToSuperview().inset(30)
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
    
    Driver.merge(tapRecognizer.rx.event.asDriver().map { _ in () },
                 backgroundDarkView.rx.tap)
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = backButton.rx.tap.asDriver()
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
