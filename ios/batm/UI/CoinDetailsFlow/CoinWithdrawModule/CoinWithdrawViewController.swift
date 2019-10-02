import UIKit
import RxCocoa
import RxSwift
import SnapKit
import QRCodeReader

final class CoinWithdrawViewController: ModuleViewController<CoinWithdrawPresenter>, QRCodeReaderViewControllerDelegate {
  
  private let didScanAddressRelay = PublishRelay<String?>()
  
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
    label.text = localize(L.CoinWithdraw.title)
    label.textColor = .white
    label.font = .poppinsSemibold20
    return label
  }()
  
  let errorView = ErrorView()
  
  let addressLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinWithdraw.address)
    label.textColor = .slateGrey
    label.font = .poppinsSemibold14
    return label
  }()
  
  let addressTextField: MainTextField = {
    let textField = MainTextField()
    textField.textAlignment = .center
    return textField
  }()
  
  let addressActionsContainer = UIView()
  
  let pasteActionContainer = UIView()
  
  let scanActionContainer = UIView()
  
  let pasteLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .paste)
    return label
  }()
  
  let scanLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .scan)
    return label
  }()
  
  let amountLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinWithdraw.amount)
    label.textColor = .slateGrey
    label.font = .poppinsSemibold14
    return label
  }()
  
  let exchangeView = CoinWithdrawExchangeView()
  
  let maxLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .max)
    return label
  }()
  
  let nextButton: MainButton = {
    let button = MainButton()
    button.configure(for: .next)
    return button
  }()
  
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
                            errorView,
                            addressLabel,
                            addressTextField,
                            addressActionsContainer,
                            amountLabel,
                            exchangeView,
                            maxLabel,
                            nextButton,
                            backgroundDarkView,
                            codeView)
    contentView.addGestureRecognizer(tapRecognizer)
    addressActionsContainer.addSubviews(pasteActionContainer,
                                        scanActionContainer)
    pasteActionContainer.addSubview(pasteLabel)
    scanActionContainer.addSubview(scanLabel)
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
    errorView.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom).offset(10)
      $0.centerX.equalToSuperview()
    }
    addressLabel.snp.makeConstraints {
      $0.top.equalTo(backgroundImageView.snp.bottom).offset(30)
      $0.centerX.equalToSuperview()
    }
    addressTextField.snp.makeConstraints {
      $0.top.equalTo(addressLabel.snp.bottom).offset(15)
      $0.left.right.equalToSuperview().inset(25)
    }
    addressActionsContainer.snp.makeConstraints {
      $0.top.equalTo(addressTextField.snp.bottom)
      $0.left.right.equalTo(addressTextField)
    }
    pasteActionContainer.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    scanActionContainer.snp.makeConstraints {
      $0.top.right.bottom.equalToSuperview()
      $0.left.equalTo(pasteActionContainer.snp.right)
      $0.width.equalTo(pasteActionContainer)
    }
    [pasteLabel, scanLabel].forEach {
      $0.snp.makeConstraints {
        $0.top.bottom.equalToSuperview().inset(15)
        $0.centerX.equalToSuperview()
      }
    }
    amountLabel.snp.makeConstraints {
      $0.top.equalTo(addressActionsContainer.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
    }
    exchangeView.snp.makeConstraints {
      $0.top.equalTo(amountLabel.snp.bottom).offset(15)
      $0.left.right.equalToSuperview().inset(25)
    }
    maxLabel.snp.makeConstraints {
      $0.top.equalTo(exchangeView.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
    }
    nextButton.snp.makeConstraints {
      $0.top.equalTo(maxLabel.snp.bottom).offset(30)
      $0.width.equalToSuperview().multipliedBy(0.42)
      $0.centerX.equalToSuperview()
    }
    backgroundDarkView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    codeView.snp.makeConstraints {
      $0.left.right.equalToSuperview().inset(30)
      $0.bottom.equalToSuperview().inset(view.safeAreaInsets.bottom + 30)
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
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [exchangeView] in exchangeView.configure(with: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.address }
      .bind(to: addressTextField.rx.text)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.currencyAmount }
      .bind(to: exchangeView.currencyTextField.rx.text)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmount }
      .bind(to: exchangeView.coinTextField.rx.text)
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
      .filter { $0 }
      .drive(onNext: { [unowned self] _ in self.showCodeView() })
      .disposed(by: disposeBag)
    
    Driver.merge(tapRecognizer.rx.event.asDriver().map { _ in () },
                 backgroundDarkView.rx.tap,
                 nextButton.rx.tap.asDriver())
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
    
    scanLabel.rx.tap
      .drive(onNext: { [unowned self] in self.showQrReader() })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = backButton.rx.tap.asDriver()
    let updateAddressDriver = Driver.merge(addressTextField.rx.text.asDriver(),
                                           didScanAddressRelay.asDriver(onErrorJustReturn: ""))
    let updateCurrencyAmountDriver = exchangeView.currencyTextField.rx.text.asDriver()
    let updateCoinAmountDriver = exchangeView.coinTextField.rx.text.asDriver()
    let pasteAddressDriver = pasteLabel.rx.tap
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let cancelDriver = codeView.rx.cancelTap
    let maxDriver = maxLabel.rx.tap
    let nextDriver = nextButton.rx.tap.asDriver()
    let sendCodeDriver = codeView.rx.nextTap
    
    presenter.bind(input: CoinWithdrawPresenter.Input(back: backDriver,
                                                      updateAddress: updateAddressDriver,
                                                      updateCurrencyAmount: updateCurrencyAmountDriver,
                                                      updateCoinAmount: updateCoinAmountDriver,
                                                      pasteAddress: pasteAddressDriver,
                                                      updateCode: updateCodeDriver,
                                                      cancel: cancelDriver,
                                                      max: maxDriver,
                                                      next: nextDriver,
                                                      sendCode: sendCodeDriver))
  }
  
  // MARK: QRReader
  
  lazy var qrReaderVC: QRCodeReaderViewController = {
    let builder = QRCodeReaderViewControllerBuilder {
      $0.reader = QRCodeReader(metadataObjectTypes: [.qr], captureDevicePosition: .back)
    }
    
    return QRCodeReaderViewController(builder: builder)
  }()
  
  private func showQrReader() {
    qrReaderVC.delegate = self
    present(qrReaderVC, animated: true, completion: nil)
  }
  
  func reader(_ reader: QRCodeReaderViewController, didScanResult result: QRCodeReaderResult) {
    reader.stopScanning()
    dismiss(animated: true, completion: nil)
    
    didScanAddressRelay.accept(result.value)
  }
  
  func readerDidCancel(_ reader: QRCodeReaderViewController) {
    reader.stopScanning()
    dismiss(animated: true, completion: nil)
  }
}
