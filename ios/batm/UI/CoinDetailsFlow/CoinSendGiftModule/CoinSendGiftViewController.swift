import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents
import GiphyUISDK
import GiphyCoreSDK

final class CoinSendGiftViewController: NavigationScreenViewController<CoinSendGiftPresenter> {
  
  let errorView = ErrorView()
  
  let headerView = CoinWithdrawHeaderView()
  
  let formView = CoinSendGiftFormView()
  
  let nextButton = MDCButton.next
  
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
  
  private let didUpdateImageRelay = PublishRelay<GPHMedia?>()
  
  private var didUpdateImageDriver: Driver<GPHMedia?> {
    return didUpdateImageRelay.asDriver(onErrorJustReturn: nil)
  }
  
  private var handler: KeyboardHandler!
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    view.addSubviews(backgroundDarkView,
                     codeView)
    
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
                                       nextButton)
    
    setupKeyboardHandling()
  }
  
  private func setupKeyboardHandling() {
    handler = KeyboardHandler(with: view)
    setupDefaultKeyboardHandling(with: handler)
  }

  override func setupLayout() {
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
      $0.bottom.lessThanOrEqualToSuperview().offset(-30)
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
      .distinctUntilChanged()
      .drive(onNext: { [customView] in
        customView.setTitle(String(format: localize(L.CoinSendGift.title), $0))
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coinBalance }
      .filterNil()
      .drive(onNext: { [headerView] in headerView.configure(for: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [formView] in formView.configure(with: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.phone }
      .bind(to: formView.rx.phoneText)
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
      .asObservable()
      .map { $0.code }
      .bind(to: codeView.smsCodeTextField.rx.text)
      .disposed(by: disposeBag)
    
    didUpdateImageDriver
      .asObservable()
      .bind(to: formView.rx.gifMedia)
      .disposed(by: disposeBag)
    
    formView.rx.addGifTap
      .drive(onNext: { [unowned self] in
        let gphVC = GiphyViewController()
        gphVC.delegate = self
        self.present(gphVC, animated: true, completion: nil)
      })
      .disposed(by: disposeBag)
    
    formView.rx.removeGifTap
      .drive(onNext: { [unowned self] in self.didUpdateImageRelay.accept(nil) })
      .disposed(by: disposeBag)
    
    let errorMessageDriverObservable = presenter.state.asObservable()
      .map { $0.validationState }
      .mapToErrorMessage()
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
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateCountryDriver = formView.rx.country
    let updatePhoneDriver = formView.rx.phoneText.asDriver()
    let updateCurrencyAmountDriver = formView.rx.currencyText.asDriver()
    let updateCoinAmountDriver = formView.rx.coinText.asDriver()
    let updateMessageDriver = formView.rx.messageText.asDriver()
    let updateImageIdDriver = didUpdateImageDriver.map { $0?.id }
    let pastePhoneDriver = formView.rx.pasteTap
    let maxDriver = formView.rx.maxTap
    let nextDriver = nextButton.rx.tap.asDriver()
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let cancelDriver = codeView.rx.cancelTap
    let sendCodeDriver = codeView.rx.nextTap
    
    presenter.bind(input: CoinSendGiftPresenter.Input(back: backDriver,
                                                      updateCountry: updateCountryDriver,
                                                      updatePhone: updatePhoneDriver,
                                                      updateCurrencyAmount: updateCurrencyAmountDriver,
                                                      updateCoinAmount: updateCoinAmountDriver,
                                                      updateMessage: updateMessageDriver,
                                                      updateImageId: updateImageIdDriver,
                                                      pastePhone: pastePhoneDriver,
                                                      max: maxDriver,
                                                      next: nextDriver,
                                                      updateCode: updateCodeDriver,
                                                      cancel: cancelDriver,
                                                      sendCode: sendCodeDriver))
  }
}

extension CoinSendGiftViewController: GiphyDelegate {
  func didSelectMedia(giphyViewController: GiphyViewController, media: GPHMedia) {
    didUpdateImageRelay.accept(media)
    
    giphyViewController.dismiss(animated: true, completion: nil)
  }
  
  func didDismiss(controller: GiphyViewController?) {}
}
