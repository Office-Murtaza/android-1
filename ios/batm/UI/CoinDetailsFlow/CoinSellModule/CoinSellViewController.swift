import UIKit
import RxCocoa
import RxSwift
import SnapKit
import QRCodeReader

final class CoinSellViewController: NavigationScreenViewController<CoinSellPresenter> {
  
  let errorView = ErrorView()
  
  let limitView = CoinSellLimitView()
  
  let exchangeView = CoinWithdrawExchangeView(sellMode: true)
  
  let anotherAddressView = CoinSellAnotherAddressView()
  
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
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       limitView,
                                       exchangeView,
                                       anotherAddressView,
                                       nextButton)
    view.addSubviews(backgroundDarkView,
                     codeView)
    
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
    limitView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(30)
      $0.left.right.equalToSuperview().inset(25)
    }
    exchangeView.snp.makeConstraints {
      $0.top.equalTo(limitView.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(25)
    }
    anotherAddressView.snp.makeConstraints {
      $0.top.equalTo(exchangeView.snp.bottom).offset(35)
      $0.centerX.equalToSuperview()
    }
    nextButton.snp.makeConstraints {
      $0.top.equalTo(anotherAddressView.snp.bottom).offset(30)
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
      .distinctUntilChanged()
      .drive(onNext: { [customView] in
        customView.setTitle(String(format: localize(L.CoinSell.title), $0))
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.details }
      .filterNil()
      .bind(to: limitView.rx.limits)
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.coin?.type.code }
      .filterNil()
      .drive(onNext: { [exchangeView] in exchangeView.configure(with: $0) })
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
    let updateFromAnotherAddressDriver = anotherAddressView.rx.isAccepted
    let updateCurrencyAmountDriver = exchangeView.rx.currencyText
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let cancelDriver = codeView.rx.cancelTap
    let maxDriver = exchangeView.rx.maxTap
    let nextDriver = nextButton.rx.tap.asDriver()
    let sendCodeDriver = codeView.rx.nextTap
    
    presenter.bind(input: CoinSellPresenter.Input(back: backDriver,
                                                  updateFromAnotherAddress: updateFromAnotherAddressDriver,
                                                  updateCurrencyAmount: updateCurrencyAmountDriver,
                                                  updateCode: updateCodeDriver,
                                                  cancel: cancelDriver,
                                                  max: maxDriver,
                                                  next: nextDriver,
                                                  sendCode: sendCodeDriver))
  }
}
