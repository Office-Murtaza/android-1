import UIKit
import RxCocoa
import RxSwift
import SnapKit
import QRCodeReader

final class CoinWithdrawViewController: NavigationScreenViewController<CoinWithdrawPresenter>, QRCodeReaderViewControllerDelegate {
  
  private let didScanAddressRelay = PublishRelay<String?>()
  
  let errorView = ErrorView()
  
  let addressView = CoinWithdrawAddressView()
  
  let exchangeView = CoinWithdrawExchangeView()
  
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
  
  private var handler: KeyboardHandler!
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    view.addSubviews(backgroundDarkView,
                     codeView)
    
    customView.contentView.addSubviews(errorView,
                                       addressView,
                                       exchangeView,
                                       nextButton)
    customView.setTitle(localize(L.CoinWithdraw.title))
   
    setupKeyboardHandling()
  }
  
  private func setupKeyboardHandling() {
    handler = KeyboardHandler(with: view)
    setupDefaultKeyboardHandling(with: handler)
  }

  override func setupLayout() {
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(10)
      $0.centerX.equalToSuperview()
    }
    addressView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.left.right.equalToSuperview().inset(25)
    }
    exchangeView.snp.makeConstraints {
      $0.top.equalTo(addressView.snp.bottom).offset(15)
      $0.left.right.equalToSuperview().inset(25)
    }
    nextButton.snp.makeConstraints {
      $0.top.equalTo(exchangeView.snp.bottom).offset(30)
      $0.width.equalToSuperview().multipliedBy(0.42)
      $0.centerX.equalToSuperview()
      $0.bottom.equalToSuperview().offset(-30)
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
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [exchangeView] in exchangeView.configure(with: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.address }
      .bind(to: addressView.rx.text)
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
    
    Driver.merge(backgroundDarkView.rx.tap,
                 nextButton.rx.tap.asDriver())
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
    
    addressView.rx.scanTap
      .drive(onNext: { [unowned self] in self.showQrReader() })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateAddressDriver = Driver.merge(addressView.rx.text.asDriver(),
                                           didScanAddressRelay.asDriver(onErrorJustReturn: ""))
    let updateCurrencyAmountDriver = exchangeView.rx.currencyText
    let updateCoinAmountDriver = exchangeView.rx.coinText
    let pasteAddressDriver = addressView.rx.pasteTap
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let cancelDriver = codeView.rx.cancelTap
    let maxDriver = exchangeView.rx.maxTap
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
