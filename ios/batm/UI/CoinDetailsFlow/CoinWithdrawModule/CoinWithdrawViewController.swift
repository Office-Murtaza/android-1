import UIKit
import RxCocoa
import RxSwift
import SnapKit
import QRCodeReader
import MaterialComponents

final class CoinWithdrawViewController: NavigationScreenViewController<CoinWithdrawPresenter>, QRCodeReaderViewControllerDelegate {
  
  private let didScanAddressRelay = PublishRelay<String?>()
  
  let errorView = ErrorView()
  
  let headerView = HeaderView()
  
  let formView = CoinWithdrawFormView()
  
  let nextButton = MDCButton.next
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
                                       nextButton)
    
    setupDefaultKeyboardHandling()
  }

  override func setupLayout() {
    customView.contentView.snp.makeConstraints {
      $0.height.equalToSuperview()
    }
    errorView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(5)
      $0.centerX.equalToSuperview()
    }
    headerView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(25)
      $0.left.equalToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(headerView.snp.bottom).offset(20)
      $0.left.right.equalToSuperview().inset(15)
    }
    nextButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.top.equalTo(formView.snp.bottom).offset(15)
      $0.left.right.equalToSuperview().inset(15)
    }
  }
  
  func setupUIBindings() {
    presenter.state
      .map { $0.coinBalance }
      .filterNil()
      .drive(onNext: { [headerView] coinBalance in
        let amountView = CryptoFiatAmountView()
        amountView.configure(for: coinBalance)
        
        headerView.removeAll()
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withUSD)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [customView] in
        customView.setTitle(String(format: localize(L.CoinWithdraw.title), $0))
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [formView] in formView.configure(with: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.address }
      .bind(to: formView.rx.addressText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.currencyAmount }
      .bind(to: formView.rx.currencyText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmount }
      .bind(to: formView.rx.coinText)
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
    
    formView.rx.scanTap
      .drive(onNext: { [unowned self] in self.showQrReader() })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateAddressDriver = Driver.merge(formView.rx.addressText.asDriver(),
                                           didScanAddressRelay.asDriver(onErrorJustReturn: ""))
    let updateCurrencyAmountDriver = formView.rx.currencyText.asDriver()
    let updateCoinAmountDriver = formView.rx.coinText.asDriver()
    let pasteAddressDriver = formView.rx.pasteTap
    let maxDriver = formView.rx.maxTap
    let nextDriver = nextButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinWithdrawPresenter.Input(back: backDriver,
                                                      updateAddress: updateAddressDriver,
                                                      updateCurrencyAmount: updateCurrencyAmountDriver,
                                                      updateCoinAmount: updateCoinAmountDriver,
                                                      pasteAddress: pasteAddressDriver,
                                                      max: maxDriver,
                                                      next: nextDriver))
  }
  
  // MARK: QRReader
  
  lazy var qrReaderVC: QRCodeReaderViewController = {
    let builder = QRCodeReaderViewControllerBuilder {
      $0.reader = QRCodeReader(metadataObjectTypes: [.qr], captureDevicePosition: .back)
    }
    let vc = QRCodeReaderViewController(builder: builder)
    vc.modalPresentationStyle = .fullScreen
    return vc
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
