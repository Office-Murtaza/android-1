import UIKit
import RxCocoa
import RxSwift
import SnapKit
import QRCodeReader
import MaterialComponents

final class CoinWithdrawViewController: ModuleViewController<CoinWithdrawPresenter>, QRCodeReaderViewControllerDelegate {
  
  private let didScanAddressRelay = PublishRelay<String?>()
  
  let rootScrollView = RootScrollView()
  
  let headerView = HeaderView()
  
  let formView = CoinWithdrawFormView()
  
  let submitButton = MDCButton.submit
  
  override var shouldShowNavigationBar: Bool { return true }
  
  override func setupUI() {
    view.addSubview(rootScrollView)
    rootScrollView.contentView.addSubviews(headerView,
                                           formView,
                                           submitButton)
    
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
    headerView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(25)
      $0.left.equalToSuperview().offset(15)
      $0.right.lessThanOrEqualToSuperview().offset(-15)
    }
    formView.snp.makeConstraints {
      $0.top.equalTo(headerView.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(15)
    }
    submitButton.snp.makeConstraints {
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
      $0.bottom.equalToSuperview().offset(-40)
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
        headerView.add(title: localize(L.CoinDetails.price), value: coinBalance.price.fiatFormatted.withDollarSign)
        headerView.add(title: localize(L.CoinDetails.balance), valueView: amountView)
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [unowned self] in
        self.title = String(format: localize(L.CoinWithdraw.title), $0)
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
      .map { $0.coinAmount }
      .bind(to: formView.rx.coinAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fiatAmount }
      .bind(to: formView.rx.fiatAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.addressError }
      .bind(to: formView.rx.addressErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.coinAmountError }
      .bind(to: formView.rx.coinAmountErrorText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.isAllFieldsNotEmpty }
      .bind(to: submitButton.rx.isEnabled)
      .disposed(by: disposeBag)
    
    submitButton.rx.tap.asDriver()
      .drive(onNext: { [view] in view?.endEditing(true) })
      .disposed(by: disposeBag)
    
    formView.rx.scanTap
      .drive(onNext: { [unowned self] in self.showQrReader() })
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let updateAddressDriver = Driver.merge(formView.rx.addressText.asDriver(),
                                           didScanAddressRelay.asDriver(onErrorJustReturn: ""))
    let updateCoinAmountDriver = formView.rx.coinAmountText.asDriver()
    let pasteAddressDriver = formView.rx.pasteTap
    let maxDriver = formView.rx.maxTap
    let submitDriver = submitButton.rx.tap.asDriver()
    
    presenter.bind(input: CoinWithdrawPresenter.Input(updateAddress: updateAddressDriver,
                                                      updateCoinAmount: updateCoinAmountDriver,
                                                      pasteAddress: pasteAddressDriver,
                                                      max: maxDriver,
                                                      submit: submitDriver))
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
