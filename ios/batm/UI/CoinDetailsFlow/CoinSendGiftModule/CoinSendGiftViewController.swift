import UIKit
import RxCocoa
import RxSwift
import SnapKit
import GiphyUISDK
import GiphyCoreSDK

final class CoinSendGiftViewController: NavigationScreenViewController<CoinSendGiftPresenter> {
  
  let errorView = ErrorView()
  
  let phoneLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinSendGift.Form.Phone.title)
    label.textColor = .slateGrey
    label.font = .poppinsSemibold14
    return label
  }()
  
  let phoneTextField = PhoneNumberTextField()
  
  let pasteLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .paste)
    return label
  }()
  
  let exchangeView = CoinWithdrawExchangeView()
  
  let gifViewContainer = UIView()
  
  let addGifLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .addGif)
    return label
  }()
  
  let removeGifLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .removeGif)
    return label
  }()
  
  let messageTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .message)
    return textField
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
  
  private let didUpdateImageIdRelay = PublishRelay<String?>()
  
  private var handler: KeyboardHandler!
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    view.addSubviews(backgroundDarkView,
                     codeView)
    
    customView.contentView.addSubviews(errorView,
                                       phoneLabel,
                                       phoneTextField,
                                       pasteLabel,
                                       exchangeView,
                                       gifViewContainer,
                                       addGifLabel,
                                       removeGifLabel,
                                       messageTextField,
                                       nextButton)
    
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
    phoneLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.centerX.equalToSuperview()
    }
    phoneTextField.snp.makeConstraints {
      $0.top.equalTo(phoneLabel.snp.bottom).offset(15)
      $0.left.right.equalToSuperview().inset(25)
    }
    pasteLabel.snp.makeConstraints {
      $0.top.equalTo(phoneTextField.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
    }
    exchangeView.snp.makeConstraints {
      $0.top.equalTo(pasteLabel.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(25)
    }
    gifViewContainer.snp.makeConstraints {
      $0.top.equalTo(exchangeView.snp.bottom).offset(30)
      $0.centerX.equalToSuperview()
      $0.height.equalTo(100)
    }
    addGifLabel.snp.makeConstraints {
      $0.top.equalTo(gifViewContainer.snp.bottom).offset(15)
      $0.right.equalTo(customView.contentView.snp.centerX).offset(-35)
    }
    removeGifLabel.snp.makeConstraints {
      $0.top.equalTo(gifViewContainer.snp.bottom).offset(15)
      $0.left.equalTo(customView.contentView.snp.centerX).offset(35)
    }
    messageTextField.snp.makeConstraints {
      $0.top.equalTo(addGifLabel.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(25)
    }
    nextButton.snp.makeConstraints {
      $0.top.equalTo(messageTextField.snp.bottom).offset(15)
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
    
    hideGifView()
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
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [exchangeView] in exchangeView.configure(with: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.validatablePhone.phone }
      .bind(to: phoneTextField.rx.text)
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
    
    addGifLabel.rx.tap
      .drive(onNext: { [unowned self] in
        let gphVC = GiphyViewController()
        gphVC.delegate = self
        self.present(gphVC, animated: true, completion: nil)
      })
      .disposed(by: disposeBag)
    
    removeGifLabel.rx.tap
      .drive(onNext: { [unowned self] in self.hideGifView() })
      .disposed(by: disposeBag)
  }
  
  private func showGifView(media: GPHMedia) {
    let gifView = GPHMediaView()
    
    gifViewContainer.subviews.forEach { $0.removeFromSuperview() }
    gifViewContainer.addSubview(gifView)
    gifView.snp.makeConstraints {
      $0.edges.equalToSuperview()
      $0.width.equalTo(gifView.snp.height).multipliedBy(media.aspectRatio)
    }
    
    gifView.setMedia(media, rendition: .fixedHeightSmall)
    
    didUpdateImageIdRelay.accept(media.id)
  }
  
  private func hideGifView() {
    let emptyGifView = UIView()
    emptyGifView.backgroundColor = .whiteTwo
    emptyGifView.layer.cornerRadius = 16
    
    let emptyGifImageView = UIImageView(image: UIImage(named: "send_gift"))
    emptyGifView.addSubview(emptyGifImageView)
    emptyGifImageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    
    gifViewContainer.subviews.forEach { $0.removeFromSuperview() }
    gifViewContainer.addSubview(emptyGifView)
    emptyGifView.snp.makeConstraints {
      $0.edges.equalToSuperview()
      $0.width.equalTo(emptyGifView.snp.height)
    }
    
    didUpdateImageIdRelay.accept(nil)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updatePhoneDriver = phoneTextField.rx.validatablePhoneNumber
    let updateCurrencyAmountDriver = exchangeView.currencyTextField.rx.text.asDriver()
    let updateCoinAmountDriver = exchangeView.coinTextField.rx.text.asDriver()
    let pastePhoneDriver = pasteLabel.rx.tap
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let updateMessageDriver = messageTextField.rx.text.asDriver()
    let updateImageIdDriver = didUpdateImageIdRelay.asDriver(onErrorJustReturn: nil)
    let cancelDriver = codeView.rx.cancelTap
    let maxDriver = exchangeView.rx.maxTap
    let nextDriver = nextButton.rx.tap.asDriver()
    let sendCodeDriver = codeView.rx.nextTap
    
    presenter.bind(input: CoinSendGiftPresenter.Input(back: backDriver,
                                                      updatePhone: updatePhoneDriver,
                                                      updateCurrencyAmount: updateCurrencyAmountDriver,
                                                      updateCoinAmount: updateCoinAmountDriver,
                                                      pastePhone: pastePhoneDriver,
                                                      updateCode: updateCodeDriver,
                                                      updateMessage: updateMessageDriver,
                                                      updateImageId: updateImageIdDriver,
                                                      cancel: cancelDriver,
                                                      max: maxDriver,
                                                      next: nextDriver,
                                                      sendCode: sendCodeDriver))
  }
}

extension CoinSendGiftViewController: GiphyDelegate {
  func didSelectMedia(giphyViewController: GiphyViewController, media: GPHMedia) {
    showGifView(media: media)
    
    giphyViewController.dismiss(animated: true, completion: nil)
  }
  
  func didDismiss(controller: GiphyViewController?) {}
}
