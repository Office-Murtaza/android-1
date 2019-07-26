import UIKit
import RxSwift
import RxCocoa
import SnapKit

class RecoverViewController: ModuleViewController<RecoverPresenter> {
  
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
    return imageView
  }()
  
  let logoImageView: UIImageView = {
    let imageView = UIImageView(image: UIImage(named: "login_logo"))
    imageView.contentMode = .scaleAspectFit
    return imageView
  }()
  
  let taglineLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Welcome.tagline)
    label.textColor = .warmGreyTwo
    label.font = .poppinsBold12
    return label
  }()
  
  let mainImageView = UIImageView(image: UIImage(named: "recover_main"))
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.Recover.title)
    label.textColor = .white
    label.font = .poppinsSemibold22
    return label
  }()
  
  let separatorView = GoldSeparatorView()
  
  let formView = RecoverFormView()
  
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
    
    view.backgroundColor = .whiteTwo
    
    view.addSubview(rootScrollView)
    rootScrollView.addSubview(contentView)
    contentView.addSubviews(backgroundImageView,
                            logoImageView,
                            taglineLabel,
                            mainImageView,
                            titleLabel,
                            separatorView,
                            formView,
                            backgroundDarkView,
                            codeView)
    contentView.addGestureRecognizer(tapRecognizer)
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
      $0.left.right.equalToSuperview()
      $0.centerY.equalToSuperview()
      $0.height.equalToSuperview().multipliedBy(0.66)
    }
    logoImageView.snp.makeConstraints {
      $0.top.equalTo(contentView.safeAreaLayoutGuide).offset(10)
      $0.centerX.equalToSuperview()
      $0.keepRatio(for: logoImageView)
    }
    logoImageView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
    logoImageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
    taglineLabel.snp.makeConstraints {
      $0.top.equalTo(logoImageView.snp.bottom).offset(5)
      $0.centerX.equalToSuperview()
      $0.bottom.lessThanOrEqualTo(backgroundImageView.snp.top).offset(-5).priority(.required)
    }
    mainImageView.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalTo(titleLabel.snp.top).offset(-15)
      $0.top.greaterThanOrEqualTo(backgroundImageView).offset(50).priority(.required)
      $0.keepRatio(for: mainImageView)
    }
    mainImageView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
    mainImageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
    titleLabel.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalTo(separatorView.snp.top).offset(-15)
    }
    separatorView.snp.makeConstraints {
      $0.centerX.equalToSuperview()
      $0.bottom.equalTo(formView.snp.top).offset(-15)
    }
    formView.snp.makeConstraints {
      $0.left.right.bottom.equalToSuperview().inset(30)
    }
    backgroundDarkView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    codeView.snp.makeConstraints {
      $0.left.right.bottom.equalTo(formView)
    }
  }
  
  private func showCodeView() {
    UIView.animate(withDuration: 0.3) {
      self.backgroundDarkView.alpha = 1
      self.codeView.alpha = 1
    }
  }
  
  private func setupUIBindings() {
    presenter.state
      .asObservable()
      .map { $0.password }
      .bind(to: formView.passwordTextField.rx.text)
      .disposed(by: disposeBag)
    
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
    
    let updatePhoneNumberDriver = formView.phoneNumberTextField.rx.phoneNumber
    let updatePasswordDriver = formView.passwordTextField.rx.text.asDriver()
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let cancelDriver = Driver.merge(codeView.rx.cancelTap,
                                    formView.rx.cancelTap)
    let recoverWalletDriver = formView.rx.nextTap
    let confirmCodeDriver = codeView.rx.nextTap
    presenter.bind(input: RecoverPresenter.Input(updatePhoneNumber: updatePhoneNumberDriver,
                                                 updatePassword: updatePasswordDriver,
                                                 updateCode: updateCodeDriver,
                                                 cancel: cancelDriver,
                                                 recoverWallet: recoverWalletDriver,
                                                 confirmCode: confirmCodeDriver))
  }
}