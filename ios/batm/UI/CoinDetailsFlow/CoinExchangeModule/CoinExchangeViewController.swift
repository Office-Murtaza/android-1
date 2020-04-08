import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

final class CoinExchangeViewController: NavigationScreenViewController<CoinExchangePresenter> {
  
  let errorView = ErrorView()
  
  let headerView = CoinWithdrawHeaderView()
  
  let formView = CoinExchangeFormView()
  
  let nextButton: MDCButton = {
    let button = MDCButton()
    button.setBackgroundColor(.ceruleanBlue)
    button.setTitle(localize(L.CoinWithdraw.Button.next), for: .normal)
    button.layer.cornerRadius = 4
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
  
  override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }

  override func setupUI() {
    view.addSubviews(backgroundDarkView,
                     codeView)
    
    customView.rootScrollView.canCancelContentTouches = false
    customView.contentView.addSubviews(errorView,
                                       headerView,
                                       formView,
                                       nextButton)
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
      $0.bottom.equalToSuperview().inset(25)
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
      .map { $0.fromCoinBalance }
      .filterNil()
      .drive(onNext: { [headerView] in headerView.configure(for: $0) })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { $0.fromCoin?.type.code }
      .filterNil()
      .distinctUntilChanged()
      .drive(onNext: { [customView] in
        customView.setTitle(String(format: localize(L.CoinExchange.title), $0))
      })
      .disposed(by: disposeBag)
    
    presenter.state
      .map { ($0.fromCoin?.type, $0.otherCoinBalances?.map { $0.type }) }
      .filter { $0 != nil && $1 != nil }
      .drive(onNext: { [formView] in formView.configure(for: $0!, and: $1!) })
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.fromCoinAmount }
      .bind(to: formView.rx.fromCoinAmountText)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.toCoinType }
      .filterNil()
      .bind(to: formView.rx.toCoin)
      .disposed(by: disposeBag)
    
    presenter.state
      .asObservable()
      .map { $0.toCoinAmount }
      .bind(to: formView.rx.toCoinAmountText)
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
                 nextButton.rx.tap.asDriver(),
                 formView.rx.toCoinTap)
    .drive(onNext: { [unowned self] in self.view.endEditing(true) })
    .disposed(by: disposeBag)
    
    Driver.merge(customView.tapRecognizer.rx.event.asDriver().map { _ in },
                 nextButton.rx.tap.asDriver(),
                 formView.fromCoinAmountTextField.rx.controlEvent(.editingDidBegin).asDriver())
      .asObservable()
      .map { true }
      .bind(to: formView.toCoinPickerView.rx.isHidden)
      .disposed(by: disposeBag)
  }

  override func setupBindings() {
    setupUIBindings()
    
    let backDriver = customView.backButton.rx.tap.asDriver()
    let updateFromCoinAmountDriver = formView.rx.fromCoinAmountText.asDriver()
    let updatePickerItemDriver = formView.rx.selectPickerItem
    let updateCodeDriver = codeView.smsCodeTextField.rx.text.asDriver()
    let cancelDriver = codeView.rx.cancelTap
    let maxDriver = formView.rx.maxTap
    let nextDriver = nextButton.rx.tap.asDriver()
    let sendCodeDriver = codeView.rx.nextTap
    
    presenter.bind(input: CoinExchangePresenter.Input(back: backDriver,
                                                      updateFromCoinAmount: updateFromCoinAmountDriver,
                                                      updatePickerItem: updatePickerItemDriver,
                                                      updateCode: updateCodeDriver,
                                                      cancel: cancelDriver,
                                                      max: maxDriver,
                                                      next: nextDriver,
                                                      sendCode: sendCodeDriver))
  }
}
