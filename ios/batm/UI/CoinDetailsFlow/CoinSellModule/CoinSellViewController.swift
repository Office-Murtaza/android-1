import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinSellViewController: NavigationScreenViewController<CoinSellPresenter> {
  
  let errorView = ErrorView()
  
  let headerView = CoinWithdrawHeaderView()
  
  let formView = CoinSellFormView()
  
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
  
  private var handler: KeyboardHandler!
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  override func setupUI() {
    customView.rootScrollView.contentInsetAdjustmentBehavior = .never
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
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
      $0.top.equalTo(formView.snp.bottom).offset(30)
      $0.left.right.equalToSuperview().inset(15)
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
      .map { $0.coinBalance }
      .filterNil()
      .drive(onNext: { [headerView] in headerView.configure(for: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.details }
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
    let updateFromAnotherAddressDriver = formView.rx.isAnotherAddress
    let updateCurrencyAmountDriver = formView.rx.currencyText.asDriver(onErrorDriveWith: .empty())
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let cancelDriver = codeView.rx.cancelTap
    let maxDriver = formView.rx.maxTap
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
